/*
 * TupleUtilsTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for TupleUtils.
 *
 * Date: 2026/02/02
 */

package io.github.open_dataspaces.core.infrastructure.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.infrastructure.dto.Tuple;

/**
 * Unit tests for the {@link TupleUtils} class.
 */
class TupleUtilsTest {

    private static String resource(String path) {
        return Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + path;
    }

    private static String operatorPath(String operatorId) {
        return ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId);
    }

    private static String operatorPlantPath(String operatorId) {
        return ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
    }

    private static String plantPath(String plantId) {
        return ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId);
    }

    /**
     * Test the buildOperatorDefaultTuples method to ensure it generates the correct list of tuples for a given operator ID.
     */
    @Test
    void testBuildOperatorDefaultTuples() {
        String operatorId = "operator-1";

        List<Tuple> expected = List.of(
            new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_OPERATOR + operatorId),
            new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_GROUP + Const.TUPLE_REL_USERS_GROUP_NAME),
            new Tuple(Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_MANAGERS_GROUP_NAME, Const.TUPLE_REL_ADMIN, Const.TUPLE_PREFIX_OPERATOR + operatorId),
            new Tuple(Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_SU_GROUP_NAME, Const.TUPLE_REL_ADMIN, Const.TUPLE_PREFIX_OPERATOR + operatorId),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, resource(operatorPath(operatorId))),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, resource(operatorPlantPath(operatorId))),
            new Tuple(Const.TUPLE_PREFIX_ROOT_OPERATOR_API, Const.TUPLE_REL_PARENT, resource(operatorPath(operatorId))),
            new Tuple(Const.TUPLE_PREFIX_GROUP + Const.TUPLE_REL_USERS_GROUP_NAME, Const.TUPLE_REL_RESOURCE_GROUP_GET, resource(operatorPath(operatorId))),
            new Tuple(Const.TUPLE_PREFIX_GROUP + Const.TUPLE_REL_MANAGERS_GROUP_NAME, Const.TUPLE_REL_RESOURCE_GROUP_STATUSUP, resource(operatorPath(operatorId)))
        );

        List<Tuple> actual = TupleUtils.buildOperatorDefaultTuples(operatorId);
        assertEquals(expected, actual, "Tuple list should match default operator tuples");
    }

    /**
     * Test the buildOperatorDefaultTuplesForManagingOperator method to ensure it generates the correct list of tuples for a given managing operator ID.
     */
    @Test
    void testBuildOperatorDefaultTuplesForManagingOperator() {
        String operatorId = "operator-2";

        List<Tuple> expected = List.of(
            new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_OPERATOR + operatorId),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_MANAGERS_GROUP_NAME),
            new Tuple(Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_SU_GROUP_NAME, Const.TUPLE_REL_ADMIN, Const.TUPLE_PREFIX_OPERATOR + operatorId),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, resource(operatorPath(operatorId))),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, resource(operatorPlantPath(operatorId))),
            new Tuple(Const.TUPLE_PREFIX_ROOT_OPERATOR_API, Const.TUPLE_REL_PARENT, resource(operatorPath(operatorId)))
        );

        List<Tuple> actual = TupleUtils.buildOperatorDefaultTuplesForManagingOperator(operatorId);
        assertEquals(expected, actual, "Tuple list should match managing operator tuples");
    }

    /**
     * Test the buildOperatorSuTuples method to ensure it generates the correct list of tuples for a given superuser operator ID.
     */
    @Test
    void testBuildOperatorSuTuples() {
        String operatorId = "operator-3";

        List<Tuple> expected = List.of(
            new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_OPERATOR + operatorId),
            new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_SU_GROUP_NAME),
            new Tuple(Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_SU_GROUP_NAME, Const.TUPLE_REL_ADMIN, Const.TUPLE_PREFIX_OPERATOR + operatorId),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, resource(operatorPath(operatorId))),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, resource(operatorPlantPath(operatorId))),
            new Tuple(Const.TUPLE_PREFIX_ROOT_OPERATOR_API, Const.TUPLE_REL_PARENT, resource(operatorPath(operatorId)))
        );

        List<Tuple> actual = TupleUtils.buildOperatorSuTuples(operatorId);
        assertEquals(expected, actual, "Tuple list should match superuser operator tuples");
    }

    /**
     * Test the buildPlantDefaultTuplesForUserOperator method to ensure it generates the correct list of tuples for a given user operator ID and plant ID.
     */
    @Test
    void testBuildPlantDefaultTuplesForUserOperator() {
        String operatorId = "operator-4";
        String plantId = "plant-1";

        List<Tuple> expected = List.of(
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_PARENT_OPERATOR, Const.TUPLE_PREFIX_PLANT + plantId),
            new Tuple(Const.TUPLE_PREFIX_PLANT + plantId, Const.TUPLE_REL_RESOURCE_PLANT, resource(plantPath(plantId))),
            new Tuple(Const.TUPLE_PREFIX_GROUP + Const.TUPLE_REL_MANAGERS_GROUP_NAME, Const.TUPLE_REL_RESOURCE_GROUP_STATUSUP, resource(plantPath(plantId))),
            new Tuple(Const.TUPLE_PREFIX_ROOT_PLANT_API, Const.TUPLE_REL_PARENT, resource(plantPath(plantId))),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_PLANT_OPERATOR, resource(plantPath(plantId)))
        );

        List<Tuple> actual = TupleUtils.buildPlantDefaultTuplesForUserOperator(operatorId, plantId);
        assertEquals(expected, actual, "Tuple list should match user operator plant tuples");
    }

    /**
     * Test the buildPlantDefaultTuplesForManagingOperator method to ensure it generates the correct list of tuples for a given managing operator ID and plant ID.
     */
    @Test
    void testBuildPlantDefaultTuplesForManagingOperator() {
        String operatorId = "operator-5";
        String plantId = "plant-2";

        List<Tuple> expected = List.of(
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_PARENT_OPERATOR, Const.TUPLE_PREFIX_PLANT + plantId),
            new Tuple(Const.TUPLE_PREFIX_PLANT + plantId, Const.TUPLE_REL_RESOURCE_PLANT, resource(plantPath(plantId))),
            new Tuple(Const.TUPLE_PREFIX_ROOT_PLANT_API, Const.TUPLE_REL_PARENT, resource(plantPath(plantId))),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_PLANT_OPERATOR, resource(plantPath(plantId)))
        );

        List<Tuple> actual = TupleUtils.buildPlantDefaultTuplesForManagingOperator(operatorId, plantId);
        assertEquals(expected, actual, "Tuple list should match managing operator plant tuples");
    }

    /**
     * Test the buildPlantDefaultTuplesForSuOperator method to ensure it generates the correct list of tuples for a given superuser operator ID and plant ID.
     */
    @Test
    void testBuildPlantDefaultTuplesForSuOperator() {
        String operatorId = "operator-6";
        String plantId = "plant-3";

        List<Tuple> expected = List.of(
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_PARENT_OPERATOR, Const.TUPLE_PREFIX_PLANT + plantId),
            new Tuple(Const.TUPLE_PREFIX_PLANT + plantId, Const.TUPLE_REL_RESOURCE_PLANT, resource(plantPath(plantId))),
            new Tuple(Const.TUPLE_PREFIX_ROOT_PLANT_API, Const.TUPLE_REL_PARENT, resource(plantPath(plantId))),
            new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_PLANT_OPERATOR, resource(plantPath(plantId)))
        );

        List<Tuple> actual = TupleUtils.buildPlantDefaultTuplesForSuOperator(operatorId, plantId);
        assertEquals(expected, actual, "Tuple list should match superuser operator plant tuples");
    }

    /**
     * Test the buildRealmStoreBindingTuples method to ensure it generates the correct list of tuples for a given realm and store ID.
     */
    @Test
    void testBuildRealmStoreBindingTuples() {
        String realm = "realm-1";
        String storeId = "store-1";

        List<Tuple> expected = List.of(new Tuple(Const.OPENFGA_EVALUATION_SUBJECT_TYPE_REALM + ":" + realm,
                Const.AUTHZ_ACTION_BOUND_TO, Const.OPENFGA_EVALUATION_RESOURCE_TYPE_STORE + ":" + storeId));

        List<Tuple> actual = TupleUtils.buildRealmStoreBindingTuples(realm, storeId);
        assertEquals(expected, actual);
    }
}
