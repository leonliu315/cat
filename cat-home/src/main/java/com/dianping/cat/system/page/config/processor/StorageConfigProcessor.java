/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.storage.builder.StorageCacheBuilder;
import com.dianping.cat.consumer.storage.builder.StorageRPCBuilder;
import com.dianping.cat.consumer.storage.builder.StorageSQLBuilder;
import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;
import com.dianping.cat.report.alert.storage.cache.StorageCacheRuleConfigManager;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCRuleConfigManager;
import com.dianping.cat.report.alert.storage.sql.StorageSQLRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class StorageConfigProcessor extends BaseProcesser {

	@Inject
	private StorageSQLRuleConfigManager m_sqlConfigManager;

	@Inject
	private StorageRPCRuleConfigManager m_rpcConfigManager;

	@Inject
	private StorageCacheRuleConfigManager m_cacheConfigManager;

	public void process(Action action, Payload payload, Model model) {
		StorageRuleConfigManager configManager = buildConfigManager(payload);

		switch (action) {
		case STORAGE_RULE:
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		case STORAGE_RULE_ADD_OR_UPDATE:
			generateRuleConfigContent(payload.getRuleId(), configManager, model);
			break;
		case STORAGE_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(configManager, payload.getRuleId(), "", payload.getConfigs()));
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		case STORAGE_RULE_DELETE:
			model.setOpState(deleteRule(configManager, payload.getRuleId()));
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		default:
			throw new RuntimeException("Error action name: " + action.getName());
		}
	}

	private StorageRuleConfigManager buildConfigManager(Payload payload) {
		String type = payload.getType();

		if (StorageCacheBuilder.ID.equals(type)) {
			return m_cacheConfigManager;
		} else if (StorageRPCBuilder.ID.equals(type)) {
			return m_rpcConfigManager;
		} else if (StorageSQLBuilder.ID.equals(type)) {
			return m_sqlConfigManager;
		} else {
			throw new RuntimeException("Error type name: " + type);
		}
	}
}
