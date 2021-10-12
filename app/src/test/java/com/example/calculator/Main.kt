package com.example.calculator

import com.example.calculator.expressionpackage.Expression

fun main(){
    val expression = Expression("5/0")
    println(expression.postfixExpression)
    println(expression.result)
}