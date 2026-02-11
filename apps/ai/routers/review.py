# TODO: 데이터 저장하기(mongo or mysql)
import requests
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter(
  prefix = "/review",
  tags = ['review']
)

class review_request(BaseModel):
  movie_id: int
  review_count: int = 100

@router.post("/crawl")
async def crawl_data(request: review_request):
  url = "https://gateway.kinolights.com/graphql"

  headers = {
      "User-Agent": "Mozilla/5.0", 
      "Content-Type": "application/json",
  }

  payload = {
      "operationName": "QueryContentReviews",
      "variables": {
          "contentId": request.movie_id, # 영화 ID
          "reviewsOffset": 0,
          "reviewsLimit": request.review_count,
          "reviewsOrderBy": "LIKE",
          "reviewsOrderOption": "DESC",
      },
      "query": """
      query QueryContentReviews(
          $contentId: Int!, 
          $reviewsOffset: Int = 0, 
          $reviewsLimit: Int = 10, 
          $reviewsOrderBy: ReviewMoviesOrderType!, 
          $reviewsOrderOption: OrderOptionType!, 
          $reviewType: ReviewFilterType, 
        ) {
        reviews(
          movieId: $contentId
          offset: $reviewsOffset
          limit: $reviewsLimit
          orderBy: $reviewsOrderBy
          orderOption: $reviewsOrderOption
          reviewType: $reviewType
        ) {
          reviewTitle
          review
          
        }
      }
      """,
  }
  try:
    response = requests.post(url, headers=headers, json=payload)
    response.raise_for_status() # HTTP 에러 발생 시 예외 처리

    data = response.json()['data']['reviews']

    if "errors" in data:
      return {
        "status" : response.status_code,
        "success" : "false",
        "message" : f"리뷰 요청에 실패했습니다. {response.text}"
      }
      
    return {
      "status": 200,
      "success": "true",
      "message": "리뷰 데이터가 저장되었습니다.",
    }

  except requests.exceptions.RequestException as e:
    return {
        "status": 500,
        "success": "false",
        "message": f"요청 실패: {str(e)}"
    }

  except ValueError:
      return {
          "status": 500,
          "success": "false",
          "message": "서버 응답을 JSON으로 파싱할 수 없습니다."
      }

  except Exception as e:
      return {
          "status": 500,
          "success": "false",
          "message": f"알 수 없는 오류 발생: {str(e)}"
      }