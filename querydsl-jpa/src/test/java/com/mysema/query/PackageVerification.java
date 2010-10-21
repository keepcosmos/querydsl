package com.mysema.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.persistence.Entity;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.mysema.codegen.CodeWriter;
import com.mysema.query.apt.hibernate.HibernateAnnotationProcessor;
import com.mysema.query.apt.jpa.JPAAnnotationProcessor;
import com.mysema.query.types.Expression;

public class PackageVerification {
    
    @Test
    public void Verify_Package() throws ClassNotFoundException, IOException{
        String version = System.getProperty("version");
        verify(new File("target/querydsl-jpa-"+version+"-apt-hibernate-one-jar.jar"), true);
        verify(new File("target/querydsl-jpa-"+version+"-apt-one-jar.jar"), false);        
    }

    private void verify(File oneJar, boolean hibernateDeps) throws ClassNotFoundException, IOException {
        assertTrue(oneJar.getPath() + " doesn't exist", oneJar.exists());
        // verify classLoader
        URLClassLoader oneJarClassLoader = new URLClassLoader(new URL[]{oneJar.toURI().toURL()});
        oneJarClassLoader.loadClass(Expression.class.getName()); // querydsl-core
        oneJarClassLoader.loadClass(CodeWriter.class.getName()); // codegen
        oneJarClassLoader.loadClass(Entity.class.getName()); // jpa        
        Class<?> processor;
        if (hibernateDeps){
            oneJarClassLoader.loadClass(org.hibernate.annotations.Type.class.getName()); // hibernate
            processor = HibernateAnnotationProcessor.class;
        }else{
            processor = JPAAnnotationProcessor.class;
        }
        oneJarClassLoader.loadClass(processor.getName()); // querydsl-apt
        String resourceKey = "META-INF/services/javax.annotation.processing.Processor";
        assertEquals(processor.getName(), IOUtils.toString(oneJarClassLoader.findResource(resourceKey).openStream()));
    }
    
}
