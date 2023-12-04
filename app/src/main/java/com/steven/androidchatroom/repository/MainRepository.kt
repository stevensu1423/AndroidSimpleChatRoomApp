package com.steven.androidchatroom.repository

import com.steven.androidchatroom.web.ApiInterface
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class MainRepository @Inject constructor(private val apiInterface: ApiInterface, private val coroutineContext: CoroutineContext): Repository {

    suspend fun login(account: String, password: String) = flow {
        emit(apiInterface.login(account, password))
    }.flowOn(coroutineContext)

    suspend fun getMyFriends(memberId: String) = flow {
        emit(apiInterface.getMyFriends(memberId))
    }.flowOn(coroutineContext)

    suspend fun getMyFriendsRequest(memberId: String) = flow {
        emit(apiInterface.getMyFriendRequest(memberId))
    }.flowOn(coroutineContext)

    suspend fun sendFriendRequest(myMemberId: String, myName: String, email: String) = flow {
        emit(apiInterface.addFriendRequest(myMemberId, myName, email))
    }.flowOn(coroutineContext)

    suspend fun friendConfirm(memberId: String, requestId: String, myName: String, requestName: String) = flow {
        emit(apiInterface.friendConfirm(memberId, requestId, myName, requestName))
    }.flowOn(coroutineContext)
}