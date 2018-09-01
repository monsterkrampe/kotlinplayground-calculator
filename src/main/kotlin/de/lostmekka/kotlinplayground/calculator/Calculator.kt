package de.lostmekka.kotlinplayground.calculator

import de.lostmekka.kotlinplayground.calculator.*

enum class Operator(val operand: Char) {
    PLUS('+'),
    MINUS('-'),
    TIMES('*'),
    DIVIDE('/');

    fun isLineOperator() = this == PLUS || this == MINUS
}

interface Node

data class NumNode(var nodeValue: Double) : Node

data class OperatorNode(var nodeValue: Operator, val leftSubTree: ParseTree, val rightSubTree: ParseTree) : Node

class ParseTree(val node: Node) {
    fun evaluate(): Double {
        return when (node) {
            is NumNode -> node.nodeValue
            is OperatorNode -> {
                when (node.nodeValue) {
                    Operator.PLUS -> node.leftSubTree.evaluate() + node.rightSubTree.evaluate()
                    Operator.MINUS -> node.leftSubTree.evaluate() - node.rightSubTree.evaluate()
                    Operator.TIMES -> node.leftSubTree.evaluate() * node.rightSubTree.evaluate()
                    Operator.DIVIDE -> {
                        val rightEvaluation = node.rightSubTree.evaluate()
                        if (rightEvaluation == 0.0) throw EvaluateException()
                        node.leftSubTree.evaluate() / rightEvaluation
                    }
                }
            }
            else -> throw EvaluateException()
        }
    }
}

class Parser() {
    private val lineOperators = Operator.values().filter { it.isLineOperator() }.map { it.operand }
    private val pointOperators = Operator.values().filter { !it.isLineOperator() }.map { it.operand }

    fun findIndexOfLastEvaluatedOperator(formula: String): Int {
        // look for operator that will be evaulated last
        var lineOperatorIndex = -1
        var pointOperatorIndex = -1

        var paranthesesCount = 0
        for ((index, element) in formula.withIndex()) {
            when {
                element == '(' -> paranthesesCount++
                element == ')' -> paranthesesCount--
                element in pointOperators && paranthesesCount == 0 -> {
                    pointOperatorIndex = index
                }
                element in lineOperators && paranthesesCount == 0 && (index == 0 || !(formula[index - 1] in pointOperators) && !(formula[index - 1] in lineOperators)) -> {
                    lineOperatorIndex = index
                }
            }
        }

        // line operators are evaluated after point operators, so we put the in tree first
        var operatorIndex = -1
        if (pointOperatorIndex > 0 && pointOperatorIndex < formula.length - 1) operatorIndex = pointOperatorIndex

        if (lineOperatorIndex > 0 && lineOperatorIndex < formula.length - 1) operatorIndex = lineOperatorIndex

        return operatorIndex
    }

    fun buildParseTreeFromFormula(formula: String): ParseTree {
        var trimmedFormula = formula

        // throw handle empty string and leading +
        if (trimmedFormula.length == 0 || trimmedFormula.first() == Operator.PLUS.operand) throw ParseException()

        // formula is only number?
        val doubleValue = trimmedFormula.toDoubleOrNull()
        if (doubleValue != null) {
            return ParseTree(NumNode(doubleValue))
        }

        if (trimmedFormula[0] == Operator.MINUS.operand) {
            trimmedFormula = "0" + trimmedFormula
        }

        val operatorIndex = findIndexOfLastEvaluatedOperator(trimmedFormula)

        if (operatorIndex > 0) {
            val operation = Operator.values().find {it.operand == trimmedFormula[operatorIndex]}
            if (operation != null) {
                return ParseTree(
                    OperatorNode(
                        operation, 
                        buildParseTreeFromFormula(trimmedFormula.substring(0, operatorIndex)),
                        buildParseTreeFromFormula(trimmedFormula.substring(operatorIndex + 1, trimmedFormula.length))
                    )
                )
            }
        }

        // if there was no operator outside all parantheses, remove parantheses
        if (trimmedFormula.first() == '(' && trimmedFormula.last() == ')') {
            return buildParseTreeFromFormula(trimmedFormula.substring(1, trimmedFormula.length - 1))
        }

        // if nothing worked, the formula is invalid
        throw ParseException()
    }
}

class Calculator : ICalculator {
    private val parser = Parser()

    fun parseFormula(formula: String): ParseTree {
        val trimmedFormula = formula.replace("""\s""".toRegex(), "")
        return parser.buildParseTreeFromFormula(trimmedFormula)
    }

    fun evaluateParseTree(parseTree: ParseTree): Double {
        return parseTree.evaluate()
    }

    override fun evaluate(formula: String): Double {
        return evaluateParseTree(parseFormula(formula))
    }
}
