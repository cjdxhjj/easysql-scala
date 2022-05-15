package org.easysql.ast.expr

enum SqlSubQueryPredicate(val predicate: String) {
    case EXISTS extends SqlSubQueryPredicate("EXISTS")
    case NOT_EXISTS extends SqlSubQueryPredicate("NOT EXISTS")
    case ANY extends SqlSubQueryPredicate("ANY")
    case ALL extends SqlSubQueryPredicate("ALL")
    case SOME extends SqlSubQueryPredicate("SOME")
}
