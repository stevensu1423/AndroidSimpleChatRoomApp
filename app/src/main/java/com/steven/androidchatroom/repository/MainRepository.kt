package com.steven.androidchatroom.repository

import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.web.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import okhttp3.Dispatcher
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class MainRepository @Inject constructor(private val apiInterface: ApiInterface, private val coroutineContext: CoroutineContext): Repository {

    fun login(account: String, password: String) = apiInterface.login(account, password)

}