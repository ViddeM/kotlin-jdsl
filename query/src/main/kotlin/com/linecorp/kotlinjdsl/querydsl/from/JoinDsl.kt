package com.linecorp.kotlinjdsl.querydsl.from

import com.linecorp.kotlinjdsl.query.spec.expression.ColumnSpec
import com.linecorp.kotlinjdsl.query.spec.expression.EntitySpec
import com.linecorp.kotlinjdsl.query.spec.predicate.EqualExpressionSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.EqualValueSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.OrSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import javax.persistence.criteria.JoinType
import kotlin.reflect.KClass

interface JoinDsl : AssociateDsl, TreatDsl {
    fun on(predicate: PredicateSpec): PredicateSpec = predicate
    fun on(predicate: () -> PredicateSpec): PredicateSpec = on(predicate())

    fun <T> on(joinColA: ColumnSpec<T>, joinColB: ColumnSpec<T>, joinType: JoinType): PredicateSpec {
        val nullableColA = ColumnSpec<T?>(joinColA.entity, joinColA.path)
        val nullableColB = ColumnSpec<T?>(joinColB.entity, joinColB.path)
        val colsEqual = EqualExpressionSpec(joinColA, joinColB)
        return when (joinType) {
            JoinType.INNER ->  colsEqual
            JoinType.LEFT -> OrSpec(listOf(colsEqual, EqualValueSpec(nullableColB, null)))
            JoinType.RIGHT -> OrSpec(listOf(colsEqual, EqualValueSpec(nullableColA, null)))
        }
    }

    fun <T, R> join(
        left: EntitySpec<T>,
        right: EntitySpec<R>,
        relation: Relation<T, R?>,
        joinType: JoinType = JoinType.INNER
    )

    fun <T : Any, R> join(
        left: KClass<T>,
        right: EntitySpec<R>,
        relation: Relation<T, R?>,
        joinType: JoinType = JoinType.INNER
    ) = join(EntitySpec(left.java), right, relation, joinType)

    fun <T, R : Any> join(
        left: EntitySpec<T>,
        right: KClass<R>,
        relation: Relation<T, R?>,
        joinType: JoinType = JoinType.INNER
    ) = join(left, EntitySpec(right.java), relation, joinType)

    fun <T : Any, R : Any> join(
        left: KClass<T>,
        right: KClass<R>,
        relation: Relation<T, R?>,
        joinType: JoinType = JoinType.INNER
    ) = join(EntitySpec(left.java), EntitySpec(right.java), relation, joinType)

    fun <T> join(entity: EntitySpec<T>, predicate: PredicateSpec)
    fun <T : Any> join(entity: KClass<T>, predicate: PredicateSpec) = join(EntitySpec(entity.java), predicate)
}
