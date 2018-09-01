package de.lostmekka.kotlinplayground.calculator

private val operators = Operator.values().map { it.operand }

data class LastOperatorResult(val lastOperatorIndex: Int, val lastOperator: Operator?)
private fun String.findIndexOfLastEvaluatedOperator(): LastOperatorResult {
  // look for operator that will be evaulated last
  var lastOperatorIndex = -1

  var paranthesesCount = 0
  var lastOperator: Operator? = null;
  for ((index, element) in this.withIndex()) {
      when {
          element == '(' -> paranthesesCount++
          element == ')' -> paranthesesCount--
          (
              element in operators 
              && paranthesesCount == 0 
              && (index == 0 || !(this[index - 1] in operators))
          ) -> {
              val currentOperator = Operator.values().find {it.operand == element}
              if (currentOperator != null && (lastOperator == null || currentOperator <= lastOperator)) {
                  lastOperator = currentOperator
                  lastOperatorIndex = index
              }
          }
      }
  }
  
  return LastOperatorResult(lastOperatorIndex, lastOperator)
}

fun String.removeWhitespaces() = this.replace("""\s""".toRegex(), "")

fun String.toParseTree(): ParseTree {
  var formula = this

  // throw handle empty string and leading +
  if (formula.length == 0 || formula.first() == Operator.PLUS.operand) throw ParseException()

  // formula is only number?
  val doubleValue = formula.toDoubleOrNull()
  if (doubleValue != null) {
      return ParseTree(NumNode(doubleValue))
  }

  if (formula[0] == Operator.MINUS.operand) {
      formula = "0" + formula
  }

  val (operatorIndex, operation) = formula.findIndexOfLastEvaluatedOperator()

  if (operatorIndex > 0 && operation != null) {
      return ParseTree(
          OperatorNode(
              operation, 
              formula.substring(0, operatorIndex).toParseTree(),
              formula.substring(operatorIndex + 1, formula.length).toParseTree()
          )
      )
  }

  // if there was no operator outside all parantheses, remove parantheses
  if (formula.first() == '(' && formula.last() == ')') {
      return formula.substring(1, formula.length - 1).toParseTree()
  }

  // if nothing worked, the formula is invalid
  throw ParseException()
}
