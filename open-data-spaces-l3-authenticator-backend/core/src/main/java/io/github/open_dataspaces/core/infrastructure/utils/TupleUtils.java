/*
 * TupleUtils.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Helper utilities to build tuple payloads for authorization store.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.infrastructure.dto.Tuple;

/**
 * Utility class for building tuple payloads.
 */
public final class TupleUtils {
    private TupleUtils() {}

    /**
     * Builds default tuples for a new operator (user operator).
     *
     * @param operatorId the operator ID
     * @return list of tuples
     */
    public static List<Tuple> buildOperatorDefaultTuples(String operatorId) {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_OPERATOR + operatorId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_GROUP + Const.TUPLE_REL_USERS_GROUP_NAME));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_MANAGERS_GROUP_NAME, Const.TUPLE_REL_ADMIN, Const.TUPLE_PREFIX_OPERATOR + operatorId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_SU_GROUP_NAME, Const.TUPLE_REL_ADMIN, Const.TUPLE_PREFIX_OPERATOR + operatorId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ROOT_OPERATOR_API, Const.TUPLE_REL_PARENT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_GROUP + Const.TUPLE_REL_USERS_GROUP_NAME, Const.TUPLE_REL_RESOURCE_GROUP_GET, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_GROUP + Const.TUPLE_REL_MANAGERS_GROUP_NAME, Const.TUPLE_REL_RESOURCE_GROUP_STATUSUP, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId)));
        return tuples;
    }

    /**
     * Builds admin-user tuples for a new operator (managing operator).
     *
     * @param operatorId the operator ID
     * @return list of tuples
     */
    public static List<Tuple> buildOperatorDefaultTuplesForManagingOperator(String operatorId) {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_OPERATOR + operatorId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_MANAGERS_GROUP_NAME));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_SU_GROUP_NAME, Const.TUPLE_REL_ADMIN, Const.TUPLE_PREFIX_OPERATOR + operatorId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ROOT_OPERATOR_API, Const.TUPLE_REL_PARENT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId)));
        return tuples;
    }

    /**
     * Builds su-user tuples for a new operator.
     *
     * @param operatorId the operator ID
     * @return list of tuples
     */
    public static List<Tuple> buildOperatorSuTuples(String operatorId) {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_OPERATOR + operatorId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_USER + operatorId, Const.TUPLE_REL_MEMBER, Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_SU_GROUP_NAME));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ADMIN + Const.TUPLE_REL_SU_GROUP_NAME, Const.TUPLE_REL_ADMIN, Const.TUPLE_PREFIX_OPERATOR + operatorId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ROOT_OPERATOR_API, Const.TUPLE_REL_PARENT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId)));
        return tuples;
    }

    /**
     * Builds default tuples for a new plant associated with an user operator.
     *
     * @param operatorId the operator ID
     * @param plantId the plant ID
     * @return list of tuples
     */
    public static List<Tuple> buildPlantDefaultTuplesForUserOperator(String operatorId, String plantId) {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_PARENT_OPERATOR, Const.TUPLE_PREFIX_PLANT + plantId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_PLANT + plantId, Const.TUPLE_REL_RESOURCE_PLANT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_GROUP + Const.TUPLE_REL_MANAGERS_GROUP_NAME, Const.TUPLE_REL_RESOURCE_GROUP_STATUSUP, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ROOT_PLANT_API, Const.TUPLE_REL_PARENT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_PLANT_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        return tuples;
    }

    /**
     * Builds default tuples for a new plant associated with an manege operator.
     *
     * @param operatorId the operator ID
     * @param plantId the plant ID
     * @return list of tuples
     */
    public static List<Tuple> buildPlantDefaultTuplesForManagingOperator(String operatorId, String plantId) {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_PARENT_OPERATOR, Const.TUPLE_PREFIX_PLANT + plantId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_PLANT + plantId, Const.TUPLE_REL_RESOURCE_PLANT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ROOT_PLANT_API, Const.TUPLE_REL_PARENT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_PLANT_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        return tuples;
    }

    /**
     * Builds default tuples for a new plant associated with an su operator.
     *
     * @param operatorId the operator ID
     * @param plantId the plant ID
     * @return list of tuples
     */
    public static List<Tuple> buildPlantDefaultTuplesForSuOperator(String operatorId, String plantId) {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_PARENT_OPERATOR, Const.TUPLE_PREFIX_PLANT + plantId));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_PLANT + plantId, Const.TUPLE_REL_RESOURCE_PLANT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_ROOT_PLANT_API, Const.TUPLE_REL_PARENT, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        tuples.add(new Tuple(Const.TUPLE_PREFIX_OPERATOR + operatorId, Const.TUPLE_REL_RESOURCE_PLANT_OPERATOR, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE + ":" + ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId)));
        return tuples;
    }

    /**
     * Builds tuples to bind a realm to a store.
     *
     * @param realm the realm
     * @param storeId the store ID
     * @return list of tuples
     */
    public static @NonNull List<Tuple> buildRealmStoreBindingTuples(@NonNull String realm, @NonNull String storeId) {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(new Tuple(Const.OPENFGA_EVALUATION_SUBJECT_TYPE_REALM + ":" + realm, Const.AUTHZ_ACTION_BOUND_TO, Const.OPENFGA_EVALUATION_RESOURCE_TYPE_STORE + ":" + storeId));
        return tuples;
    }
}