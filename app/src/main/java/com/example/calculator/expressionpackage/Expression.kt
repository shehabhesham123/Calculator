package com.example.calculator.expressionpackage

import android.widget.TextView
import java.util.*

// when enter negative number ,, it must is at ()    like (-3)
class Expression(private val infixExpression: String) {

    private enum class Operation {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION,
        PARENTHESES;

        companion object {
            fun priority(operation: Operation): Int {
                return when (operation) {
                    ADDITION -> 1
                    SUBTRACTION -> 1
                    MULTIPLICATION -> 2
                    DIVISION -> 2
                    PARENTHESES -> 0
                }
            }

            fun fromCharToOperationEnum(operation: Char): Operation {
                return when (operation) {
                    '+' -> ADDITION
                    '-' -> SUBTRACTION
                    '*' -> MULTIPLICATION
                    '/' -> DIVISION
                    '(', ')' -> PARENTHESES
                    else -> throw Exception("Invalid Operation")
                }
            }

            fun isOperation(operationParam: Char): Pair<Boolean, Operation?> {
                val operation = when (operationParam) {
                    '+' -> ADDITION
                    '-' -> SUBTRACTION
                    '*' -> MULTIPLICATION
                    '/' -> DIVISION
                    else -> return Pair(false, null)
                }
                return Pair(true, operation)
            }

        }
    }

    val postfixExpression: String = infixToPostfix()    // بتتنفذ ف ال constructor

    private fun infixToPostfix(): String {

        var operationStack: Stack<Char> = Stack()
        var postfix = ""

        var number = ""

        for ((j, i) in infixExpression.withIndex()) {

            /////////////////////////////// special case /////////////////////////////////
            if (i == '(') operationStack.push(i)
            else if (i == ')') {
                postfix += checkNumber(number)
                number = ""

                // هنا بيمسح كل العمليات اللى موجوده ف ال Stack لحد مالاقى ال )
                // وبيرجع حاجتين ال Stack المعتدل عشان اعدل القديم
                // وال operations اللى شلناها من ال Stack عشان اضفها فال postfix string
                val (operations, stack) = popOperationsFromStack(operationStack)
                postfix += operations
                operationStack = stack
            }
            //////////////////////////////////////////////////////////////////////////////
            else if (i != ' ') {
                if (isNumber(i) || i == '.') number += i
                // in case 5*(-3)
                else if (i == '-' && number.isEmpty() && infixExpression[j - 1] == '(') number += '-'
                else {
                    postfix += checkNumber(number)
                    number = ""

                    if (operationStack.isEmpty()) operationStack.push(i)
                    else {
                        if (isOperationMorePriority(
                                Operation.fromCharToOperationEnum(i),
                                Operation.fromCharToOperationEnum(operationStack.peek())
                            )
                        ) {
                            operationStack.push(i)
                        } else {
                            postfix += "${operationStack.pop()} "
                            operationStack.push(i)
                        }
                    }
                }
            }
        }

        // for example -> 5+3
        // number = 3
        // stack contain +
        // عشان اخر حاجه كانت ال 3 و فالحالة دى هيخلى ال number+=3 وال loop هتخلص
        postfix += checkNumber(number)

        // pop the rest of operations
        postfix += popOperationsFromStack(operationStack).first

        return postfix
    }

    // pop operation between parentheses (special case)
    // or pop the rest of operations at end of algorithm   فممكن يكون ف عملية واحدة او اكتر من عملية ف حالة ان كل العمليات اكبر من اللى قبلها
    private fun popOperationsFromStack(operationStack: Stack<Char>): Pair<String, Stack<Char>> {
        var postfix = ""
        while (!operationStack.isEmpty()) {
            if (operationStack.peek() != '(') postfix += "${operationStack.pop()} "
            // لو كانت ب )   ساعتها بكون مناديها عشان اشيل العمليات اللى بين الاقواس وساعتها بوقف ع كده
            // غير كده بكون مناديها عشان اشيل كل العمليات البتبقية اللى ف ال Stack
            else {
                operationStack.pop()
                break
            }
        }
        //  برجع ال operationStack عشان هنا بمسح منه عناصر وال Stack الرئيسى هيكون لسا موجوده فيه فبرجعه عشان اعدل ال Stack الرئيسى
        return Pair(postfix, operationStack)
    }

    // just check
    private fun checkNumber(number: String): String {
        return if (number.isNotEmpty()) "$number "
        else ""
    }

    private fun isNumber(number: Char): Boolean {
        return (number in '0'..'9')
    }

    // return true if operation 1 more then operation 2
    // return false if operation 1 less then operation 2
    // ال operation1 بتكون هى اللى عليها الدور تخش ال Stack وال operation2 هى اللى موجوده ف ال stack
    private fun isOperationMorePriority(operation1: Operation, operation2: Operation): Boolean {
        return Operation.priority(operation1) > Operation.priority(operation2)
    }

    val result: Any
        get() {
            val stack = Stack<Double>()      // ليه double عشان ممكن يكون ف ارقام عشرية
            var number = ""
            for ((j, i) in postfixExpression.withIndex()) {
                if (i != ' ') {
                    val (isOperation, operation) = Operation.isOperation(i)
                    if (isOperation) {
                        // in case (-5)   -> infix  3*(-5)      postfix  3 -5 *
                        if (operation == Operation.SUBTRACTION && isNumber(postfixExpression[j + 1])) number += '-'
                        else {
                            val number1 = stack.pop()
                            val number2 = stack.pop()
                            when (operation) {
                                Operation.ADDITION -> stack.push(number1 + number2)
                                Operation.SUBTRACTION -> stack.push(number2 - number1)
                                Operation.DIVISION -> {
                                    if (number1 == 0.0) throw RuntimeException("Division by zero")
                                    stack.push(number2 / number1)
                                }
                                Operation.MULTIPLICATION -> stack.push(number1 * number2)
                                else -> throw Exception("Invalid Operation")
                            }
                        }
                    } else if (isNumber(i) || i == '.') {
                        number += i
                    }
                } else if (i == ' ' && number.isNotEmpty()) {
                    stack.push(number.toDouble())
                    number = ""
                }
            }

            return when (stack.size) {
                1 -> {
                    val num = stack.pop()
                    // عشان لو من رقم عشرى نشيل ال 0. من الرقم
                    if (isDouble(num)) num
                    else num.toLong()
                }
                else -> throw Exception("Invalid Expression")
            }
        }

    private fun isDouble(numParam: Double): Boolean {
        val num = numParam.toString()
        for ((i, j) in num.withIndex()) {
            if (j == '.') {
                for (h in (i + 1) until num.length) {
                    if (num[h] != '0') return true
                }
                break
            }
        }
        return false
    }
}


// بستخدم ال expressionRule ف كل حاجة ال user عايز يدخلها زى العمليات والرقام وبشوف ينفع تتكتب دلوقتى ولا لا
class ExpressionRule(private val expression: TextView) {

    private var writeDotBefore = mutableListOf(false)   // false for first number

    // 12+6       -> 12 idx 0    6  idx 1 .....              وده بيكون بيحتوى ع اخر idx
    private var idxOfNumber = 0
    private val parenthesesStack = Stack<Char>()      // عشان اشوف الاقواس مظبوطة ولا لا

    fun addSymbol(symbol: Char): String {
        val expressionAsText = expression.text.toString()
        val lastSymbol =
            if (expressionAsText.isNotEmpty()) expressionAsText[expressionAsText.length - 1] else null
        return when (symbol) {
            in '0'..'9' -> addNumberRule(lastSymbol, symbol)
            '+', '-', '*', '/' -> {
                try {
                    val o = addOperationRule(lastSymbol, symbol)
                    // اول ما اضيف operation   كده معناه انى هضيف رقم تانى وبالتالى هضيف idx جديد فى ال writeDotBefore وهخليه ب false عشان يدل انه مفهوش dot وان مسموع بالكتابة ال dot
                    writeDotBefore.add(false)
                    idxOfNumber++
                    o
                } catch (e: Exception) {
                    throw e
                }
            }
            '(', ')' -> addParenthesesRule(lastSymbol, symbol)
            '.' -> {
                val o = addDotRule(lastSymbol)
                o
            }
            else -> throw Exception("Invalid Symbol (AddSymbol)")
        }
    }

    fun clear() {
        parenthesesStack.clear()
        idxOfNumber = 0
        expression.text = ""
        writeDotBefore.clear()
        writeDotBefore.add(false)
    }

    fun delete() {
        val expressionAsText = expression.text.toString()
        if (expressionAsText.length > 1){
            val lastChar = expressionAsText[expressionAsText.length - 1]
            if (lastChar == '.')
                writeDotBefore[idxOfNumber] = false
            else if (lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/') {
                writeDotBefore[idxOfNumber] = false
                idxOfNumber--
            }
        }
    }

    fun isValid(): Boolean {
        val expressionAsText = expression.text.toString()
        if (expressionAsText.isEmpty()) return false
        val lastSymbol = expressionAsText[expressionAsText.length - 1]
        return (parenthesesStack.isEmpty() && lastSymbol != '.' && lastSymbol != '+'
                && lastSymbol != '-' && lastSymbol != '*' && lastSymbol != '/')
    }

    // if lastSymbol is null that's mean that expression is null
    private fun addNumberRule(lastSymbol: Char?, number: Char): String {
        return when (lastSymbol) {
            in '0'..'9', '.', '+', '-', '*', '/', '(', null -> "$number"
            ')' -> "*$number"
            else -> throw CanNotAdd()
        }
    }

    private fun addOperationRule(lastSymbol: Char?, operation: Char): String {
        return when (operation) {
            '+' -> additionDivisionMultiplicationRule(lastSymbol, operation)
            '-' -> subtractionRule(lastSymbol)
            '*' -> additionDivisionMultiplicationRule(lastSymbol, operation)
            '/' -> additionDivisionMultiplicationRule(lastSymbol, operation)
            else -> throw Exception("Invalid Operation (AddOperationRule)")
        }
    }

    private fun addParenthesesRule(lastSymbol: Char?, parentheses: Char): String {
        return when (parentheses) {
            ')' -> {
                try {
                    val o = rightParenthesesRule(lastSymbol)
                    parenthesesStack.pop()
                    o
                } catch (e: CanNotAdd) {
                    throw e
                }
            }
            '(' -> {
                try {
                    val o = leftParenthesesRule(lastSymbol)
                    parenthesesStack.push('(')
                    o
                } catch (e: CanNotAdd) {
                    throw e
                }
            }
            else -> throw Exception("Invalid Symbol (AddParenthesesRule)")
        }
    }

    private fun addDotRule(lastSymbol: Char?): String {
        return if (!writeDotBefore[idxOfNumber]) {
            when (lastSymbol) {
                '+', '-', '*', '/', '(', null -> {
                    writeDotBefore[idxOfNumber] = true
                    "0."
                }
                in '0'..'9' -> {
                    writeDotBefore[idxOfNumber] = true
                    "."
                }
                else -> throw CanNotAdd()
            }
        } else ""
    }

    private fun additionDivisionMultiplicationRule(lastSymbol: Char?, operation: Char): String {
        return when (lastSymbol) {
            in '0'..'9', ')' -> "$operation"
            '.' -> "0$operation"
            else -> throw CanNotAdd()
        }
    }

    private fun subtractionRule(lastSymbol: Char?): String {
        return when (lastSymbol) {
            in '0'..'9', ')' -> "-"
            '*', '/' -> {
                parenthesesStack.push('(')
                "(-"
            }
            '.' -> "0-"
            else -> throw CanNotAdd()
        }
    }

    private fun leftParenthesesRule(lastSymbol: Char?): String {
        return when (lastSymbol) {
            '+', '-', '*', '/', ')', null -> "("
            in '0'..'9' -> "*("
            '.' -> "0*"
            else -> throw CanNotAdd()
        }
    }

    private fun rightParenthesesRule(lastSymbol: Char?): String {
        if (!parenthesesStack.isEmpty()) {
            return when (lastSymbol) {
                in '0'..'9' -> ")"
                '.' -> ".0)"
                else -> throw CanNotAdd()
            }
        } else throw CanNotAdd()
    }
}

class CanNotAdd : Exception()