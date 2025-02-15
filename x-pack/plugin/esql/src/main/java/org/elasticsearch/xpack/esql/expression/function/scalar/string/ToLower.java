/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.expression.function.scalar.string;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.compute.ann.Evaluator;
import org.elasticsearch.compute.ann.Fixed;
import org.elasticsearch.compute.operator.EvalOperator.ExpressionEvaluator;
import org.elasticsearch.xpack.esql.evaluator.mapper.EvaluatorMapper;
import org.elasticsearch.xpack.esql.expression.function.FunctionInfo;
import org.elasticsearch.xpack.esql.expression.function.Param;
import org.elasticsearch.xpack.esql.session.EsqlConfiguration;
import org.elasticsearch.xpack.ql.expression.Expression;
import org.elasticsearch.xpack.ql.expression.function.scalar.ConfigurationFunction;
import org.elasticsearch.xpack.ql.expression.gen.script.ScriptTemplate;
import org.elasticsearch.xpack.ql.session.Configuration;
import org.elasticsearch.xpack.ql.tree.NodeInfo;
import org.elasticsearch.xpack.ql.tree.Source;
import org.elasticsearch.xpack.ql.type.DataType;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static org.elasticsearch.xpack.ql.expression.TypeResolutions.ParamOrdinal.DEFAULT;
import static org.elasticsearch.xpack.ql.expression.TypeResolutions.isString;

public class ToLower extends ConfigurationFunction implements EvaluatorMapper {

    private final Expression field;

    @FunctionInfo(
        returnType = { "keyword", "text" },
        description = "Returns a new string representing the input string converted to lower case."
    )
    public ToLower(
        Source source,
        @Param(name = "str", type = { "keyword", "text" }, description = "The input string") Expression field,
        Configuration configuration
    ) {
        super(source, List.of(field), configuration);
        this.field = field;
    }

    @Override
    public DataType dataType() {
        return field.dataType();
    }

    @Override
    protected TypeResolution resolveType() {
        if (childrenResolved() == false) {
            return new TypeResolution("Unresolved children");
        }

        return isString(field, sourceText(), DEFAULT);
    }

    @Override
    public boolean foldable() {
        return field.foldable();
    }

    @Override
    public Object fold() {
        return EvaluatorMapper.super.fold();
    }

    @Evaluator
    static BytesRef process(BytesRef val, @Fixed Locale locale) {
        return BytesRefs.toBytesRef(val.utf8ToString().toLowerCase(locale));
    }

    @Override
    public ExpressionEvaluator.Factory toEvaluator(Function<Expression, ExpressionEvaluator.Factory> toEvaluator) {
        var fieldEvaluator = toEvaluator.apply(field);
        return new ToLowerEvaluator.Factory(source(), fieldEvaluator, ((EsqlConfiguration) configuration()).locale());
    }

    public Expression field() {
        return field;
    }

    public ToLower replaceChild(Expression child) {
        return new ToLower(source(), child, configuration());
    }

    @Override
    public Expression replaceChildren(List<Expression> newChildren) {
        assert newChildren.size() == 1;
        return replaceChild(newChildren.get(0));
    }

    @Override
    protected NodeInfo<? extends Expression> info() {
        return NodeInfo.create(this, ToLower::new, field, configuration());
    }

    @Override
    public ScriptTemplate asScript() {
        throw new UnsupportedOperationException("functions do not support scripting");
    }
}
