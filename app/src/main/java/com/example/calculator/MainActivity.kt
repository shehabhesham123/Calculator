package com.example.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.calculator.expressionpackage.CanNotAdd
import com.example.calculator.expressionpackage.Expression
import com.example.calculator.expressionpackage.ExpressionRule


class MainActivity : AppCompatActivity() {

    private lateinit var expression: TextView
    private lateinit var resultAsTextView: TextView
    private lateinit var expressionRule: ExpressionRule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        expression = findViewById(R.id.MainActivity_TextView_Expression)
        resultAsTextView = findViewById(R.id.MainActivity_TextView_Result)
        expressionRule = ExpressionRule(expression)

        // listener for delete button
        deleteLongClick()
    }

    @SuppressLint("SetTextI18n")
    fun click(view: View) {
        try {
            val char: String = when (view.id) {
                R.id.MainActivity_Button_Number0 -> expressionRule.addSymbol('0')
                R.id.MainActivity_Button_Number1 -> expressionRule.addSymbol('1')
                R.id.MainActivity_Button_Number2 -> expressionRule.addSymbol('2')
                R.id.MainActivity_Button_Number3 -> expressionRule.addSymbol('3')
                R.id.MainActivity_Button_Number4 -> expressionRule.addSymbol('4')
                R.id.MainActivity_Button_Number5 -> expressionRule.addSymbol('5')
                R.id.MainActivity_Button_Number6 -> expressionRule.addSymbol('6')
                R.id.MainActivity_Button_Number7 -> expressionRule.addSymbol('7')
                R.id.MainActivity_Button_Number8 -> expressionRule.addSymbol('8')
                R.id.MainActivity_Button_Number9 -> expressionRule.addSymbol('9')
                R.id.MainActivity_Button_LeftParentheses -> expressionRule.addSymbol('(')
                R.id.MainActivity_Button_RightParentheses -> expressionRule.addSymbol(')')
                R.id.MainActivity_Button_Addition -> expressionRule.addSymbol('+')
                R.id.MainActivity_Button_Subtraction -> expressionRule.addSymbol('-')
                R.id.MainActivity_Button_Multiplication -> expressionRule.addSymbol('*')
                R.id.MainActivity_Button_Division -> expressionRule.addSymbol('/')
                R.id.MainActivity_Button_Dot -> expressionRule.addSymbol('.')
                else -> ""
            }
            expression.text = "${expression.text}$char"

            // to make HorizontalScrollView focus of end of expression always
            val scrollbar: HorizontalScrollView = findViewById(R.id.scrollview)
            scrollbar.postDelayed({
                scrollbar.fullScroll(
                    HorizontalScrollView.FOCUS_RIGHT
                )
            }, 100L)

        } catch (e: CanNotAdd) {
            Toast.makeText(baseContext, "Can't Add", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(baseContext, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    fun equalClick(view: View) {
        val expressionObj = Expression(expression.text.toString())
        try {
            if (expressionRule.isValid()) {
                resultAsTextView.text = expressionObj.result.toString()
            } else resultAsTextView.text = "Invalid"
        } catch (e: RuntimeException) {   // division by 0
            resultAsTextView.text = "Invalid"
        }
    }

    fun clearClick(view: View) {
        resultAsTextView.text = ""
        expressionRule.clear()
    }

    fun deleteClick(view: View) {
        val expressionAsText = expression.text.toString()
        if (expressionAsText.isNotEmpty()) {
            val newExpression =
                expressionAsText.removeRange(expressionAsText.length - 1, expressionAsText.length)
            expression.text = newExpression
            expressionRule.delete()
        }
    }

    private fun deleteLongClick() {
        val delete = findViewById<ImageView>(R.id.MainActivity_Button_Delete)
        delete.setOnLongClickListener {
            clearClick(View(baseContext))
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("Expression",expression.text.toString())
        outState.putString("Result",resultAsTextView.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        expression.text = savedInstanceState.getString("Expression")
        resultAsTextView.text = savedInstanceState.getString("Result")
    }
}

