package com.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var inputString = ""
    private var outputString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        // Di sini kita mendefinisikan tombol-tombol kalkulator dan operasinya.
        // Menggunakan binding untuk menghubungkan setiap tombol dengan aksi yang sesuai.
        val buttons = mapOf(
            binding.button0 to "0",
            binding.button1 to "1",
            binding.button2 to "2",
            binding.button3 to "3",
            binding.button4 to "4",
            binding.button5 to "5",
            binding.button6 to "6",
            binding.button7 to "7",
            binding.button8 to "8",
            binding.button9 to "9",
            binding.buttonAdd to "+",
            binding.buttonSubtract to "-",
            binding.buttonMultiply to "*",
            binding.buttonDivide to "/",
            binding.buttonClear to "C",
            binding.buttonEquals to "="
        )

        // Setiap tombol dihubungkan dengan handleButtonClick() untuk memproses input.
        for ((button, value) in buttons) {
            button.setOnClickListener {
                handleButtonClick(value) // Memanggil fungsi handleButtonClick saat tombol diklik
            }
        }

        // Tombol backspace untuk menghapus karakter terakhir dari inputString.
        binding.buttonBackspace.setOnClickListener {
            if (inputString.isNotEmpty()) {
                // Menghapus karakter terakhir dari inputString
                inputString = inputString.dropLast(1)
                updateDisplay() // Memperbarui tampilan setelah perubahan
            }
        }
    }

    private fun handleButtonClick(value: String) {
        // Mengatur aksi berdasarkan nilai tombol yang diklik
        when (value) {
            "C" -> {
                // Menghapus semua input dan output ketika tombol Clear diklik
                inputString = ""
                outputString = ""
            }
            "=" -> {
                // Menghitung hasil dari ekspresi yang diinputkan ketika tombol Sama dengan diklik
                outputString = try {
                    calculateExpression(inputString).toString()
                } catch (e: Exception) {
                    "Error" // Menampilkan "Error" jika terjadi kesalahan dalam perhitungan
                }
            }
            else -> {
                // Tombol angka atau operasi: Menambahkan input ke inputString.
                inputString += value
            }
        }
        updateDisplay() // Memperbarui tampilan setelah setiap klik tombol
    }

    private fun updateDisplay() {
        // Fungsi untuk memperbarui tampilan operasi dan hasil di layar kalkulator.
        binding.displayOperation.text = inputString
        binding.displayResult.text = outputString
    }

    private fun calculateExpression(expression: String): Double {
        // Fungsi untuk menghitung ekspresi matematika menggunakan metode infix to postfix.
        return try {
            val expr = expression.replace("x", "*") // Mengganti 'x' dengan '*' untuk operasi perkalian
            eval(expr) // Memanggil fungsi eval untuk menghitung hasil
        } catch (e: Exception) {
            throw ArithmeticException("Invalid expression") // Melempar kesalahan jika ekspresi tidak valid
        }
    }

    private fun eval(expression: String): Double {
        // Menghitung hasil dari ekspresi yang diberikan
        val tokens = tokenize(expression) // Memecah ekspresi menjadi token-token
        val rpn = infixToPostfix(tokens) // Mengonversi dari notasi infix ke postfix
        return evaluateRPN(rpn) // Menghitung hasil dari notasi postfix
    }

    private fun tokenize(expression: String): List<String> {
        // Tokenize: Memecah ekspresi menjadi angka dan operator.
        return expression.replace(" ", "").split("(?<=[-+*/()])|(?=[-+*/()])".toRegex())
    }

    private fun infixToPostfix(tokens: List<String>): List<String> {
        // Mengonversi ekspresi dari notasi infix ke notasi postfix menggunakan algoritma shunting yard
        val precedence = mapOf('+' to 1, '-' to 1, '*' to 2, '/' to 2) // Prioritas operator
        val output = mutableListOf<String>() // Daftar untuk menyimpan hasil konversi ke postfix
        val operators = mutableListOf<Char>() // Stack untuk menyimpan operator

        // Iterasi melalui setiap token dalam ekspresi
        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token) // Jika token adalah angka, tambahkan ke output
                token in precedence.keys.map { it.toString() } -> {
                    // Jika token adalah operator, keluarkan operator dari stack ke output sesuai prioritas
                    while (operators.isNotEmpty() && precedence[operators.last()]!! >= precedence[token[0]]!!) {
                        output.add(operators.removeAt(operators.size - 1).toString())
                    }
                    operators.add(token[0]) // Tambahkan operator saat ini ke stack
                }
                token == "(" -> operators.add('(') // Jika token adalah '(', tambahkan ke stack operator
                token == ")" -> {
                    // Jika token adalah ')', keluarkan semua operator sampai menemukan '('
                    while (operators.isNotEmpty() && operators.last() != '(') {
                        output.add(operators.removeAt(operators.size - 1).toString())
                    }
                    operators.removeAt(operators.size - 1) // Hapus '(' dari stack
                }
            }
        }

        // Setelah iterasi, keluarkan semua operator yang tersisa di stack ke output
        while (operators.isNotEmpty()) {
            output.add(operators.removeAt(operators.size - 1).toString())
        }

        return output // Kembalikan daftar token dalam notasi postfix
    }

    private fun evaluateRPN(tokens: List<String>): Double {
        // Menghitung hasil dari ekspresi dalam notasi postfix (RPN)
        val stack = mutableListOf<Double>() // Stack untuk menyimpan angka

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> stack.add(token.toDouble()) // Jika token adalah angka, tambahkan ke stack
                else -> {
                    // Jika token adalah operator, keluarkan dua angka dari stack dan terapkan operator
                    val b = stack.removeAt(stack.size - 1)
                    val a = stack.removeAt(stack.size - 1)
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        else -> throw ArithmeticException("Invalid operator") // Melempar kesalahan jika operator tidak valid
                    }
                    stack.add(result) // Tambahkan hasil operasi ke stack
                }
            }
        }

        return stack[0] // Hasil akhir adalah satu-satunya elemen yang tersisa di stack
    }
}
