package cn.kter.ktutil


interface MviView<S:State>{
    fun render(state:S)
}
interface State
interface MviPresent<S:State,V : MviView<S>> {
    fun bindIntents(v:V)
}
typealias Reducer<S, A> = (S, A) -> S
