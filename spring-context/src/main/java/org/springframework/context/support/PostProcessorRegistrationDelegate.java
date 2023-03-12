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


	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

// ------------------------------- 调用BeanDefinitionRegistryPostProcessor的后置处理器 Begin -------------------------------
		// 定义已处理的后置处理器
		Set<String> processedBeans = new HashSet<>();

		// 判断beanFactory实现了BeanDefinitionRegistry(实现了该接口就有注册和获取Bean定义的能力)，这里是可以进if的
		if (beanFactory instanceof BeanDefinitionRegistry) {
			// 强行把bean工厂转为BeanDefinitionRegistry，因为待会要注册Bean定义
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			// 保存BeanFactoryPostProcessor类型的后置处理器
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();

			// 保存BeanDefinitionRegistryPostProcessor类型的后置处理器
			// BeanDefinitionRegistryPostProcessor提供注册功能，扩展了BeanFactoryPostProcessor
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			// 循环我们传递进来的beanFactoryPostProcessors，如果没有手动添加是不会进这个for循环的
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				// 判断我们的后置处理器是不是BeanDefinitionRegistryPostProcessor
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					// 强制转换
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					// 调用它作为BeanDefinitionRegistryPostProcessor处理器的后置方法
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					// 添加到我们用于保存BeanDefinitionRegistryPostProcessor的集合中
					registryProcessors.add(registryProcessor);
				}
				else {
					// 若是没有实现BeanDefinitionRegistryPostProcessor接口，那么它就是BeanFactoryPostProcessor
					// 把当前的后置处理器加入到regularPostProcessors
					regularPostProcessors.add(postProcessor);
				}
			}

			// 定义一个集合用于保存当前准备创建的BeanDefinitionRegistryPostProcessor
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			/**
			 * 如果我们自己创建了一个实现了BeanDefinitionRegistryPostProcessor接口的类，也加上了@Component注解，这里获取Names的时候还是没有获取到！！
			 * 因为直到这一步，spring还没有开始扫描，扫描是在ConfigurationClassPostProcessor类中完成的，即下面的Names的第一个元素
			 *
			 * ConfigurationClassPostProcessor实现了BeanDefinitionRegistryPostProcessor接口，我们的@ComponentScan、@Import注解的解析都是由这个类完成
			 */
			// 第一步：去容器中获取BeanDefinitionRegistryPostProcessor的bean的处理器名称
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			// 循环筛选出来的匹配BeanDefinitionRegistryPostProcessor的类型名称
			for (String ppName : postProcessorNames) {
				// 判断是否实现了PriorityOrdered接口
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					// 显式调用getBean()方法获取出该对象然后加入到currentRegistryProcessors集合中
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 同时也加入到processedBeans集合中
					processedBeans.add(ppName);
				}
			}
			// 对currentRegistryProcessors集合中的BeanDefinitionRegistryPostProcessor进行排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			/**
			 * 合并processors，为什么要合并呢？
			 * 因为registryProcessors是存放所有BeanDefinitionRegistryPostProcessor的，
			 * spring会先执行BeanDefinitionRegistryPostProcessor独有的方法
			 * 而不会执行BeanDefinitionRegistryPostProcessor父类BeanFactoryPostProcessor的方法，
			 * 所以统一放入一个集合之后，后续统一执行父类方法
			 */
			registryProcessors.addAll(currentRegistryProcessors);
			/**
			 * 在这里典型的BeanDefinitionRegistryPostProcessor就是ConfigurationClassPostProcessor
			 * 即可以理解为执行ConfigurationClassPostProcessor的postProcessBeanDefinitionRegistry方法
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			// 调用完立马clear掉
			currentRegistryProcessors.clear();
// ------------------------------- 调用内置实现PriorityOrdered接口ConfigurationClassPostProcessor完毕 -- 优先级NO 1 -- END -------------------------------

			// 去容器中获取BeanDefinitionRegistryPostProcessor的处理器，在这里才会获取到我们自定义的处理器！！
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			// 循环上一步获取的BeanDefinitionRegistryPostProcessor的类型名称
			for (String ppName : postProcessorNames) {
				// 表示没有被处理过，且实现了Ordered接口
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					// 显式调用getBean()方法获取出该对象然后加入到currentRegistryProcessors集合中
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 同时也加入到processedBeans集合中
					processedBeans.add(ppName);
				}
			}
			// 对currentRegistryProcessors集合中的BeanDefinitionRegistryPostProcessor进行排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			// 把当前的加入到总的里面去
			registryProcessors.addAll(currentRegistryProcessors);
			// 调用后置处理方法
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			// 调用完立即clear掉
			currentRegistryProcessors.clear();
// ------------------------------- 调用自定义Ordered接口BeanDefinitionRegistryPostProcessor完毕 -- 优先级NO 2 -- END -------------------------------

			// 调用没有实现任何优先级接口的BeanDefinitionRegistryPostProcessor
			// 定义一个重复处理的开关变量 默认为true
			boolean reiterate = true;
			// 第一次可以进去
			while (reiterate) {
				// 进入循环马上将开关置为false
				reiterate = false;
				// 去容器中获取BeanDefinitionRegistryPostProcessor的bean处理器名称
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				// 循环上一步获取的BeanDefinitionRegistryPostProcessor的类型名称
				for (String ppName : postProcessorNames) {
					// 没有被处理过的
					if (!processedBeans.contains(ppName)) {
						// 显式调用getBean()方法获取出该对象然后加入到currentRegistryProcessors集合中
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						// 同时也加入到processedBeans集合中
						processedBeans.add(ppName);
						// 在此将开关设置为true
						reiterate = true;
					}
				}
				// 对currentRegistryProcessors集合中的BeanDefinitionRegistryPostProcessor进行排序
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				// 把当前的加入到总的里面去
				registryProcessors.addAll(currentRegistryProcessors);
				// 调用后置处理方法
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
				// 调用完立即clear掉
				currentRegistryProcessors.clear();
			}
// ------------------------------- 调用没有实现任何优先级接口自定义BeanDefinitionRegistryPostProcessor完毕 -- END -------------------------------

			// 调用BeanDefinitionRegistryPostProcessor.postProcessBeanFactory方法，即执行父类BeanFactoryPostProcessor的方法
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			// 调用自设的BeanFactoryPostProcessor，如果没有自定义就没有
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// 如果当前的beanFactory没有实现BeanDefinitionRegistry，说明没有注册Bean定义的能力
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}
// ------------------------------- 所有BeanDefinitionRegistryPostProcessor调用完毕 -- END -------------------------------

// ------------------------------- 处理BeanFactoryPostProcessor -- Begin -------------------------------

		// 获取容器中所有的BeanFactoryPostProcessor
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// 保存实现了PriorityOrdered接口的BeanFactoryPostProcessor
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 保存实现了Ordered接口的BeanFactoryPostProcessor
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 保存没有实现任何优先级接口的BeanFactoryPostProcessor
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			// processedBeans包含ppName说明之前已经处理过
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			// 是否实现了PriorityOrdered，优先级最高
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			// 是否实现了Ordered，优先级其次
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			// 没有实现任何优先级接口的最后调用
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 排序
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		// 先调用实现了PriorityOrdered的BeanFactoryPostProcessor
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// 再调用实现了Ordered的BeanFactoryPostProcessor
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// 调用没有实现任何优先级接口的BeanFactoryPostProcessor
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);
// ------------------------------- 处理BeanFactoryPostProcessor -- End -------------------------------

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
// ------------------------------- BeanFactoryPostProcessor和BeanDefinitionRegistryPostProcessor调用完毕 -- End -------------------------------
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		// WARNING: Although it may appear that the body of this method can be easily
		// refactored to avoid the use of multiple loops and multiple lists, the use
		// of multiple lists and multiple passes over the names of processors is
		// intentional. We must ensure that we honor the contracts for PriorityOrdered
		// and Ordered processors. Specifically, we must NOT cause processors to be
		// instantiated (via getBean() invocations) or registered in the ApplicationContext
		// in the wrong order.
		//
		// Before submitting a pull request (PR) to change this method, please review the
		// list of all declined PRs involving changes to PostProcessorRegistrationDelegate
		// to ensure that your proposal does not result in a breaking change:
		// https://github.com/spring-projects/spring-framework/issues?q=PostProcessorRegistrationDelegate+is%3Aclosed+label%3A%22status%3A+declined%22

		// 去容去中获取所有的BeanPostProcessor的名称
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		/**
		 * bean的后置处理器的个数beanFactory.getBeanPostProcessorCount()成品的个数之前refresh --> prepareBeanFactory中注册的
		 * postProcessorNames.length beanFactory工厂中的bean定义的个数 + 1
		 * 在后面马上又注册了BeanPostProcessorChecker
		 */
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
		// 把实现了PriorityOrdered注册到容器中
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
		// 注册MergedBeanDefinitionPostProcessor类型的后置处理器 bean合并后的处理，Autowired注解正是通过此方法实现诸如类型的预解析
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

		// 获取容器中的ConfigurationClassPostProcessor的后置处理器进行bean定义扫描
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
