package de.lostmekka.kotlinplayground.calculator

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
