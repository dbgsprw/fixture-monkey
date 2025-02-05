/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.traverser;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryTraverser {
	private final GenerateOptions generateOptions;

	public ArbitraryTraverser(GenerateOptions generateOptions) {
		this.generateOptions = generateOptions;
	}

	public ArbitraryNode traverse(RootProperty rootProperty) {
		ArbitraryPropertyGenerator arbitraryPropertyGenerator =
			this.generateOptions.getArbitraryPropertyGenerator(rootProperty);

		ArbitraryProperty rootArbitraryProperty = arbitraryPropertyGenerator.property(
			new ArbitraryPropertyGeneratorContext(
				rootProperty,
				null,
				null,
				null,
				this.generateOptions
			)
		);

		return this.traverse(rootArbitraryProperty);
	}

	private ArbitraryNode traverse(ArbitraryProperty arbitraryProperty) {
		List<ArbitraryNode> children = new ArrayList<>();

		List<Property> childProperties = arbitraryProperty.getChildProperties();
		for (int index = 0; index < childProperties.size(); index++) {
			Property childProperty = childProperties.get(0);
			ArbitraryPropertyGenerator arbitraryPropertyGenerator =
				this.generateOptions.getArbitraryPropertyGenerator(childProperty);
			ArbitraryProperty childArbitraryProperty = arbitraryPropertyGenerator.property(
				new ArbitraryPropertyGeneratorContext(
					childProperty,
					null,
					arbitraryProperty.getContainerInfo() != null ? index : null,
					arbitraryProperty,
					this.generateOptions
				)
			);

			ArbitraryNode childNode = this.traverse(childArbitraryProperty);
			children.add(childNode);
		}

		Arbitrary<?> arbitrary = null;
		if (arbitraryProperty.getPropertyValue() != null) {
			arbitrary = Arbitraries.ofSuppliers(() -> arbitraryProperty.getPropertyValue().get());
		}

		return new ArbitraryNode(
			arbitraryProperty,
			children,
			arbitrary
		);
	}
}
