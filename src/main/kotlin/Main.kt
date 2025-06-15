package org.example

import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis


@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    println("Hello World!")
    val time = measureTimeMillis {
        runBlocking {
            repeat(100_000) {
                launch {
                    delay(1000)
                }
            }
        }
    }
    runBlocking {
        for (i in 1..10000) {
            launch {
                exec(i)
            };
        }
    }
    println("Execution time: $time")

    println("Execution with CoRoutine scope ")
    coroutineScope {
        launch {
            execInScope(1000)
        }

        launch {
            execInScope(2000)
        }
        launch {
            execInScope(100)
        }
    }

    println("Coroutinre with exception and stop eecuting");
    coroutineScope {
        val job: Job = launch {
            try {
                execInScope(5000);
            } catch (e: CancellationException) {
                println("Job was cancelled too long exectuition of ${e.message}")
            }
        }
        launch {
            delay(500);
            job.cancel("I dont want to wait so long")
        }
    }
    println("CoRouting with async")
    coroutineScope {
        val deferred: Deferred<Double> = async {
            execAsyncWithReturn(4000)
        }
        val received = deferred.await()
        println("RECEIVED RESULT $received")
    }

    println("multiply Coroutines")
    coroutineScope {
        val deferredList = (1..3).map { i ->
            async {
                execAsyncWithReturn(i * 1000L)
            }
        }
        val doubleList = deferredList.awaitAll();
        println("Overall delay time  ${doubleList.sum()} sec.")
    }
    println("finish")

    println(" Dispatchers")
    println(" Start:")
    runBlocking {
        launch {

        }
        launch (Dispatchers.Default ){
            println("with default dispatcher")
        }
        launch (Dispatchers.IO ){
            println("with default dispatcher IO")
        }
        launch(newSingleThreadContext("MyOwnThread")) {
            println("newSingleThreadContext: ${newSingleThreadContext("MyOwnThread")}")
        }
        launch(Dispatchers.Unconfined) {
            println("unconfined")
        }

    }

    println("Coroutines with custom Thread Pool")
    println("Start")
    runBlocking {
        newFixedThreadPoolContext(5, "MyThreadPool").use { context ->

            withContext(context) {
                repeat(5) { i ->
                    launch {
                        println("Start coroutine $i")
                        delay(Random.nextLong(100L))
                        println("Stop coroutine $i")
                    }
                }
            }
        }
    }
    println("Finish")

    println("Structural Coroutines")
    println("Finish structural concurrency")
    val time2  = measureTimeMillis {
        runBlocking {
            println("Outer scope")
            val job1 = launch{
                println("Inner scope 1")
                launch{
                    println("Inner launch 1")
                    delay(2000)
                    println("It will be never be printed")
                }
                launch{
                    println("Inner launch 2")
                    delay(3000)
                    println("It will be never be printed")
                }
                GlobalScope.launch {
                    println("Global scope launch ")
                    delay(4000)
                }
            }
            launch {
                println("Innncer scope 2")
                delay(1000)
                job1.cancelAndJoin()
            }
        }
    }
    println("Execution time $time2")
    println("Finish")

    println("======================================================================================================")
    println("Change context")
    println("Start")
    runBlocking{
        newSingleThreadContext("Context_1").use { ctx1 ->
            newSingleThreadContext("Context_2").use { ctx2 ->
                withContext(ctx1) {
                    println("Start in context 1")
                    withContext(ctx2) {
                        println("Work in context 2")
                    }
                    println("End in context 1")
                }
            }
        }
    }

    println("======================================================================================================")
    println("End CoRoutine  with timeout")
    runBlocking {
        withTimeout(1000) {
            println("Launch 1")
            delay(2000)
            println("It will never be printed")
        }
    }
    println("FINISH")
}

private suspend fun exec(i: Int) {
    println("Start do something: $i")
    delay(1000)
    println("End do something: $i")
}

private suspend fun execInScope(ms: Long) {
    println("coroutineScope Start do something: $ms")
    delay(ms)
    println("coroutineScope End do something: $ms")
}

private suspend fun execAsyncWithReturn(ms: Long): Double {
    println("coroutineScope async with return:")
    delay(ms)
    val result = ms.toDouble() / 1000
    println("coroutineScope End do something: $ms")
    return result;
}