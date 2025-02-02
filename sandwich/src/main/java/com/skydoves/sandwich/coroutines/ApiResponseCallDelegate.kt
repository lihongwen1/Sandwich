/*
 * Designed and developed by 2020 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.sandwich.coroutines

import com.skydoves.sandwich.ApiResponse
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class ApiResponseCallDelegate<T>(proxy: Call<T>) : CallDelegate<T, ApiResponse<T>>(proxy) {

  override fun enqueueImpl(callback: Callback<ApiResponse<T>>) = proxy.enqueue(object : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
      val apiResponse = ApiResponse.of { response }
      if (apiResponse is ApiResponse.Success) {
        callback.onResponse(this@ApiResponseCallDelegate, Response.success(apiResponse))
      } else if (apiResponse is ApiResponse.Failure.Error && apiResponse.errorBody != null) {
        callback.onResponse(this@ApiResponseCallDelegate,
          Response.error(apiResponse.statusCode.code, apiResponse.errorBody))
      }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
      callback.onResponse(this@ApiResponseCallDelegate, Response.success(ApiResponse.error(t)))
    }
  })

  override fun timeout(): Timeout = Timeout.NONE

  override fun cloneImpl() = ApiResponseCallDelegate(proxy.clone())
}
