package de.lostmekka.kotlinplayground.calculator

enum class Operator(val operand: Char) {
    PLUS('+'),
    MINUS('-'),
    TIMES('*'),
    DIVIDE('/');
}
