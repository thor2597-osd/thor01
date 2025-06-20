package com.thor.config;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.springframework.stereotype.Component;
import java.nio.file.Paths;

@Component
public class CodeGenerator {
  public static void main(String[] args) {
    FastAutoGenerator.create("jdbc:mysql://localhost:3306/second-hand?serverTime=GMT%2B8", "root", "12345678")
        .globalConfig(builder -> builder
            .author("OSD")
            .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java")
            .commentDate("yyyy-MM-dd")
        )
        .packageConfig(builder -> builder
            .parent("com.example.platform")
            .entity("entity")
            .mapper("mapper")
            .service("service")
            .serviceImpl("service.impl")
            .xml("mapper.xml")
        )
        .strategyConfig(builder -> builder
            .entityBuilder()
            .enableLombok()
        )
        .templateEngine(new FreemarkerTemplateEngine())
        .execute();
  }
}