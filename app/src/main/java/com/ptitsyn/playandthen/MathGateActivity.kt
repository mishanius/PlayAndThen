package com.ptitsyn.playandthen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MathGateActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var answerInput: EditText
    private lateinit var submitButton: Button
    
    private var correctAnswer: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_gate)
        
        questionText = findViewById(R.id.questionText)
        answerInput = findViewById(R.id.answerInput)
        submitButton = findViewById(R.id.submitButton)
        
        generateNewQuestion()
        
        submitButton.setOnClickListener {
            checkAnswer()
        }
    }
    
    private fun generateNewQuestion() {
        val num1 = Random.nextInt(10, 51)
        val num2 = Random.nextInt(10, 51)
        val isAddition = Random.nextBoolean()
        
        if (isAddition) {
            correctAnswer = num1 + num2
            questionText.text = "What is $num1 + $num2?"
        } else {
            // Ensure positive result
            val larger = maxOf(num1, num2)
            val smaller = minOf(num1, num2)
            correctAnswer = larger - smaller
            questionText.text = "What is $larger - $smaller?"
        }
        
        answerInput.text.clear()
    }
    
    private fun checkAnswer() {
        val userAnswer = answerInput.text.toString().toIntOrNull()
        
        if (userAnswer == correctAnswer) {
            startActivity(Intent(this, ParentDashboardActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
            generateNewQuestion()
        }
    }
}
