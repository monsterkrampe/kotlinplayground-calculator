package de.lostmekka.kotlinplayground.calculator

class Calculator : ICalculator {
    override fun evaluate(formula: String): Double {
        return formula.removeWhitespaces().toParseTree().evaluate()
    }
}
