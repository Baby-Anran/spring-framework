package com.spring.demo.imports;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {ImportCommonBean.class, MyImportSelect.class, MyImportBeanDefinitionRegister.class, MyDeferredImportSelect.class})
@ComponentScan(basePackages = {"com.spring.demo.imports"})
public class ImportConfig {
}
