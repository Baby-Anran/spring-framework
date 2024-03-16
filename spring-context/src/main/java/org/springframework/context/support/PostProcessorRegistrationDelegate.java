/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}

	/**
	 * BeanFactoryPostProcessors按入场方式分为：
	 * 1. 程序员调用ApplicationContext的API手动添加
	 * 2. Spring自己扫描出来的
	 * BeanFactoryPostProcessor按类型又可以分为：
	 * 1. 普通BeanFactoryPostProcessor
	 * 2. BeanDefinitionRegistryPostProcessor
	 * 执行顺序顺序如下：
	 * 1. 执行手动添加的BeanDefinitionRegistryPostProcessor							的postProcessBeanDefinitionRegistry()方法
	 * 2. 执行扫描出来的BeanDefinitionRegistryPostProcessor(实现了PriorityOrdered)	的postProcessBeanDefinitionRegistry()方法
	 * 3. 执行扫描出来的BeanDefinitionRegistryPostProcessor(实现了Ordered)			的postProcessBeanDefinitionRegistry()方法
	 * 4. 执行扫描出来的BeanDefinitionRegistryPostProcessor(普通)					的postProcessBeanDefinitionRegistry()方法
	 * 5. 执行扫描出来的BeanDefinitionRegistryPostProcessor(所有)					的postProcessBeanFactory()方法
	 * 6. 执行手动添加的BeanFactoryPostProcessor										的postProcessBeanFactory()方法
	 * 7. 执行扫描出来的BeanFactoryPostProcessor(实现了PriorityOrdered)				的postProcessBeanFactory()方法
	 * 8. 执行扫描出来的BeanFactoryPostProcessor(实现了Ordered)						的postProcessBeanFactory()方法
	 * 9. 执行扫描出来的BeanFactoryPostProcessor(普通)								的postProcessBeanFactory()方法
	 * ConfigurationClassPostProcessor就会在第2步执行，会进行扫描
	 */
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// 定义已处理的后置处理器
		Set<String> processedBeans = new HashSet<>();

		// 判断beanFactory实现了BeanDefinitionRegistry(实现了该接口就有注册和获取Bean定义的能力)，这里是可以进if的
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			// 手动添加的BeanFactoryPostProcessor
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			// beanFactoryPostProcessors集合一般情况下都是空的，除非我们手动调用容器的addBeanFactoryPostProcessor方法添加了
			// beanFactoryPostProcessors中可能包含了：普通BeanFactoryPostProcessor对象和BeanDefinitionRegistryPostProcessor对象
			// 对于BeanDefinitionRegistryPostProcessor对象，会执行自己的postProcessBeanDefinitionRegistry()方法
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					// 自己手动添加的BeanDefinitionRegistryPostProcessor先执行postProcessBeanDefinitionRegistry方法
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			/*
			 * 如果我们自己创建了一个实现了BeanDefinitionRegistryPostProcessor接口的类，也加上了@Component注解，这里获取Names的时候还是没有获取到！！
			 * 因为直到这一步，spring还没有开始扫描，扫描是在ConfigurationClassPostProcessor类中完成的
			 * ConfigurationClassPostProcessor实现了BeanDefinitionRegistryPostProcessor接口，我们的@ComponentScan、@Import注解的解析都是由这个类完成
			 */

			// 开始调用扫描出来的BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry
			// 第一步：获取实现了PriorityOrdered的BeanDefinitionRegistryPostProcessor
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			// 升序排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			// registryProcessors存放了所有的BeanDefinitionRegistryPostProcessor
			// 在执行完BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry方法之后，后续需要执行类BeanFactoryPostProcessor的的postProcessBeanFactory方法
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			currentRegistryProcessors.clear();

			// 第二步：获取实现了Ordered的BeanDefinitionRegistryPostProcessor(这里就可以获取到我们用@Component定义的处理器)
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				// processedBeans表示该beanFactoryPostProcessor的postProcessBeanDefinitionRegistry()方法已经执行过了，不再重复执行
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			currentRegistryProcessors.clear();

			// 最后执行没有实现任何优先级接口的BeanDefinitionRegistryPostProcessor
			// 在这个过程中可能会向BeanFactory中注册另外的BeanDefinitionRegistryPostProcessor，所以需要while，直到确定所有的BeanDefinitionRegistryPostProcessor都执行完了
			// 在这个过程中注册的BeanDefinitionRegistryPostProcessor，所实现的PriorityOrdered或Ordered接口可能会不按顺序执行
			// 比如 A注册了B和C,B又注册了D和E,那么B和C会按顺序执行，D和E也会按顺序执行，但是B、C、D、E整体不能保证是顺序执行
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
				currentRegistryProcessors.clear();
			}

			// 执行完BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry()方法后，
			// 再执行BeanDefinitionRegistryPostProcessor的postProcessBeanFactory()方法
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			// 执行手动添加的普通BeanFactoryPostProcessor的postProcessBeanFactory()方法
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}
		else {
			// 如果当前的beanFactory没有实现BeanDefinitionRegistry，说明没有注册Bean定义的能力
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// 获取容器中所有的BeanFactoryPostProcessor
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();

		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// 之前处理过就跳过
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		// 先调用实现了PriorityOrdered的BeanFactoryPostProcessor
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		// 调用实现了Ordered的BeanFactoryPostProcessor
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		// 调用没有实现任何优先级接口的BeanFactoryPostProcessor
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		// 去容去中获取所有的BeanPostProcessor的名称
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		// beanProcessorTargetCount表示BeanFactory中所有的BeanPostProcessor数量，+1表示BeanPostProcessorChecker
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		/**
		 * 按照BeanPostProcessor实现的优先级接口来分离我们的后置处理器
		 */
		// 保存实现了priorityOrdered接口的
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 系统内部的
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		// // 保存实现了Ordered接口的
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 没有优先级的
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		// 循环bean定义
		for (String ppName : postProcessorNames) {
			// 往上面各个list中填充数据
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		// 升序排序
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);


		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			// 显式调用getBean方法
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			// 加入到集合中
			orderedPostProcessors.add(pp);
			// 判断是否实现了MergedBeanDefinitionPostProcessor
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		// 把实现了Ordered注册到容器中
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		// 实例化所有没有实现优先级接口的后置处理器
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		// 注册没有实现优先级接口的后置处理器
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		// MergedBeanDefinitionPostProcessor排在最后
		// 注册MergedBeanDefinitionPostProcessor类型的后置处理器 bean合并后的处理，Autowired注解正是通过此方法实现注入类型的预解析
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		// 注册ApplicationListenerDetector应用监听器探测器的后置处理器
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		// Nothing to sort?
		if (postProcessors.size() <= 1) {
			return;
		}
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry, ApplicationStartup applicationStartup) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanDefRegistry = applicationStartup.start("spring.context.beandef-registry.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanDefinitionRegistry(registry);
			postProcessBeanDefRegistry.end();
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanFactory = beanFactory.getApplicationStartup().start("spring.context.bean-factory.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanFactory(beanFactory);
			postProcessBeanFactory.end();
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		if (beanFactory instanceof AbstractBeanFactory) {
			// Bulk addition is more efficient against our CopyOnWriteArrayList there
			((AbstractBeanFactory) beanFactory).addBeanPostProcessors(postProcessors);
		}
		else {
			for (BeanPostProcessor postProcessor : postProcessors) {
				beanFactory.addBeanPostProcessor(postProcessor);
			}
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
