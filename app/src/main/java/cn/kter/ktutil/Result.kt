package cn.kter.ktutil

sealed class Result{
    data class Success<T>(val v:T):Result()
    data class Error(val e:String):Result()
}
