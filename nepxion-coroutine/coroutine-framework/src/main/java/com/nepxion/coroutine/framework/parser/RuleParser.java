package com.nepxion.coroutine.framework.parser;

/**
 * <p>Title: Nepxion Coroutine</p>
 * <p>Description: Nepxion Coroutine For Distribution</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: Nepxion</p>
 * @author Neptune
 * @email 1394997@qq.com
 * @version 1.0
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nepxion.coroutine.common.constant.CoroutineConstants;
import com.nepxion.coroutine.common.util.ClassUtil;
import com.nepxion.coroutine.common.xml.Dom4JReader;
import com.nepxion.coroutine.data.cache.RuleCache;
import com.nepxion.coroutine.data.entity.ChainEntity;
import com.nepxion.coroutine.data.entity.ClassEntity;
import com.nepxion.coroutine.data.entity.ComponentEntity;
import com.nepxion.coroutine.data.entity.DependencyEntity;
import com.nepxion.coroutine.data.entity.MethodEntity;
import com.nepxion.coroutine.data.entity.RuleEntity;
import com.nepxion.coroutine.data.entity.RuleKey;
import com.nepxion.coroutine.data.entity.StepEntity;
import com.nepxion.coroutine.data.entity.StepType;
import com.nepxion.coroutine.framework.core.CoroutineManager;

public class RuleParser {
    private static final Logger LOG = LoggerFactory.getLogger(RuleParser.class);

    private RuleKey ruleKey;

    public RuleParser(RuleKey ruleKey) {
        this.ruleKey = ruleKey;
    }

    public void parse(String text) throws DocumentException {
        Document document = Dom4JReader.getDocument(text);

        parse(document);
    }

    public void parseFormat(String text) throws DocumentException, UnsupportedEncodingException {
        Document document = Dom4JReader.getFormatDocument(text);

        parse(document);
    }

    public void parse(File file) throws DocumentException, IOException, UnsupportedEncodingException {
        Document document = Dom4JReader.getDocument(file);

        parse(document);
    }

    public void parseFormat(File file) throws DocumentException, IOException, UnsupportedEncodingException {
        Document document = Dom4JReader.getFormatDocument(file);

        parse(document);
    }

    public void parse(InputStream inputStream) throws DocumentException, IOException {
        Document document = Dom4JReader.getDocument(inputStream);

        parse(document);
    }

    public void parseFormat(InputStream inputStream) throws DocumentException, IOException, UnsupportedEncodingException {
        Document document = Dom4JReader.getFormatDocument(inputStream);

        parse(document);
    }

    public void parse(Document document) {
        Element rootElement = document.getRootElement();

        parseRoot(rootElement);
    }

    @SuppressWarnings("rawtypes")
    private void parseRoot(Element element) {
        LOG.info("Start to parse rule, categoryName={}, ruleName={}", ruleKey.getCategoryName(), ruleKey.getRuleName());

        List<RuleEntity> ruleEntityList = RuleCache.getRules(ruleKey);
        if (ruleEntityList == null) {
            ruleEntityList = new CopyOnWriteArrayList<RuleEntity>();
            RuleCache.putRules(ruleKey, ruleEntityList);
        }

        for (Iterator elementIterator = element.elementIterator(); elementIterator.hasNext();) {
            Object childElementObject = elementIterator.next();
            if (childElementObject instanceof Element) {
                Element childElement = (Element) childElementObject;

                int version = Integer.parseInt(childElement.attribute(CoroutineConstants.VERSION_ATTRIBUTE_NAME).getData().toString().trim());
                if (RuleCache.isRuleExisted(ruleKey, version)) {
                    LOG.info("Rule has existed, categoryName={}, ruleName={}, version={}", ruleKey.getCategoryName(), ruleKey.getRuleName(), version);

                    break;
                }

                LOG.info("Rule is loaded, categoryName={}, ruleName={}, version={}", ruleKey.getCategoryName(), ruleKey.getRuleName(), version);

                RuleEntity ruleEntity = new RuleEntity();
                ruleEntity.setVersion(version);
                ruleEntityList.add(ruleEntity);

                parseRule(childElement, ruleEntity);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseRule(Element element, RuleEntity ruleEntity) {
        List<ComponentEntity> componentEntityList = new ArrayList<ComponentEntity>();
        ruleEntity.setComponentEntityList(componentEntityList);

        List<DependencyEntity> dependencyEntityList = new ArrayList<DependencyEntity>();
        ruleEntity.setDependencyEntityList(dependencyEntityList);

        List<ChainEntity> chainEntityList = new ArrayList<ChainEntity>();
        ruleEntity.setChainEntityList(chainEntityList);

        for (Iterator elementIterator = element.elementIterator(); elementIterator.hasNext();) {
            Object childElementObject = elementIterator.next();
            if (childElementObject instanceof Element) {
                Element childElement = (Element) childElementObject;
                if (StringUtils.equals(childElement.getName(), CoroutineConstants.COMPONENT_ATTRIBUTE_NAME)) {
                    ComponentEntity componentEntity = new ComponentEntity();

                    parseComponent(childElement, componentEntity);

                    componentEntityList.add(componentEntity);
                } else if (StringUtils.equals(childElement.getName(), CoroutineConstants.DEPENDENCY_ATTRIBUTE_NAME)) {
                    DependencyEntity dependencyEntity = new DependencyEntity();

                    parseDependency(childElement, dependencyEntity);

                    dependencyEntityList.add(dependencyEntity);
                } else if (StringUtils.equals(childElement.getName(), CoroutineConstants.CHAIN_ATTRIBUTE_NAME)) {
                    ChainEntity chainEntity = new ChainEntity();

                    parseChain(childElement, chainEntity);

                    chainEntityList.add(chainEntity);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseComponent(Element element, ComponentEntity componentEntity) {
        Attribute applicationContextAttribute = element.attribute(CoroutineConstants.APPLICATION_CONTEXT_ATTRIBUTE_NAME);
        if (applicationContextAttribute != null) {
            String applicationContextPath = applicationContextAttribute.getData().toString().trim();
            componentEntity.setApplicationContextPath(applicationContextPath);
        }

        List<ClassEntity> classEntityList = new ArrayList<ClassEntity>();
        componentEntity.setClassEntityList(classEntityList);

        for (Iterator elementIterator = element.elementIterator(); elementIterator.hasNext();) {
            Object childElementObject = elementIterator.next();
            if (childElementObject instanceof Element) {
                Element childElement = (Element) childElementObject;

                ClassEntity classEntity = new ClassEntity();

                parseClass(childElement, componentEntity, classEntity);

                classEntityList.add(classEntity);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseClass(Element element, ComponentEntity componentEntity, ClassEntity classEntity) {
        String id = null;
        Attribute idAttribute = element.attribute(CoroutineConstants.ID_ATTRIBUTE_NAME);
        if (idAttribute != null) {
            id = idAttribute.getData().toString().trim();
        }

        String clazz = element.attribute(CoroutineConstants.CLASS_ATTRIBUTE_NAME).getData().toString().trim();
        String applicationContextPath = componentEntity.getApplicationContextPath();
        ApplicationContext applicationContext = componentEntity.getApplicationContext();
        if (StringUtils.isNotEmpty(applicationContextPath) && applicationContext == null) {
            if (applicationContextPath.toLowerCase().startsWith(CoroutineConstants.CLASS_PATH_ATTRIBUTE_NAME)) {
                applicationContext = new ClassPathXmlApplicationContext(applicationContextPath);
                componentEntity.setApplicationContext(applicationContext);
            } else {
                String absoluteApplicationContextPath = null;
                try {
                    absoluteApplicationContextPath = ClassUtil.createAbsoluteApplicationContextPath(clazz, applicationContextPath);
                } catch (Exception e) {
                    LOG.error("Create absolute application context path failed", e);
                }

                applicationContext = new ClassPathXmlApplicationContext(absoluteApplicationContextPath);
                componentEntity.setApplicationContext(applicationContext);
            }
        }

        Object object = null;
        try {
            if (applicationContext == null) {
                object = ClassUtil.createInstance(clazz);
            } else {
                object = applicationContext.getBean(id);
            }
        } catch (Exception e) {
            LOG.error("Create object failed", e);
        }

        List<MethodEntity> methodEntityList = new ArrayList<MethodEntity>();
        classEntity.setId(id);
        classEntity.setClazz(clazz);
        classEntity.setMethodEntityList(methodEntityList);

        for (Iterator elementIterator = element.elementIterator(); elementIterator.hasNext();) {
            Object childElementObject = elementIterator.next();
            if (childElementObject instanceof Element) {
                Element childElement = (Element) childElementObject;

                MethodEntity methodEntity = new MethodEntity();

                parseMethod(childElement, methodEntity);

                methodEntity.setClazz(clazz);
                methodEntity.setObject(object);

                methodEntityList.add(methodEntity);
            }
        }
    }

    private void parseMethod(Element element, MethodEntity methodEntity) {
        int index = Integer.parseInt(element.attribute(CoroutineConstants.INDEX_ATTRIBUTE_NAME).getData().toString().trim());
        methodEntity.setIndex(index);

        String method = element.attribute(CoroutineConstants.METHOD_ATTRIBUTE_NAME).getData().toString().trim();
        methodEntity.setMethod(method);

        Attribute parameterTypesAttribute = element.attribute(CoroutineConstants.PARAMETER_TYPES_ATTRIBUTE_NAME);
        if (parameterTypesAttribute != null) {
            String parameterTypes = parameterTypesAttribute.getData().toString().trim();
            methodEntity.setParameterTypes(parameterTypes);
        }
        
        Attribute cacheAttribute = element.attribute(CoroutineConstants.CACHE_ATTRIBUTE_NAME);
        if (cacheAttribute != null) {
            String cache = cacheAttribute.getData().toString().trim();
            methodEntity.setCache(Boolean.valueOf(cache));
        }
    }

    private void parseDependency(Element element, DependencyEntity dependencyEntity) {
        int index = Integer.parseInt(element.attribute(CoroutineConstants.INDEX_ATTRIBUTE_NAME).getData().toString().trim());
        dependencyEntity.setIndex(index);

        String categoryName = element.attribute(CoroutineConstants.CATEGORY_ATTRIBUTE_NAME).getData().toString().trim();
        dependencyEntity.setCategoryName(categoryName);

        String ruleName = element.attribute(CoroutineConstants.RULE_ATTRIBUTE_NAME).getData().toString().trim();
        dependencyEntity.setRuleName(ruleName);

        Attribute chainNameAttribute = element.attribute(CoroutineConstants.CHAIN_ATTRIBUTE_NAME);
        if (chainNameAttribute != null) {
            String chainName = chainNameAttribute.getData().toString().trim();
            dependencyEntity.setChainName(chainName);
        }

        String filePath = null;
        Attribute filePathAttribute = element.attribute(CoroutineConstants.FILE_ATTRIBUTE_NAME);
        if (filePathAttribute != null) {
            filePath = filePathAttribute.getData().toString().trim();
        }

        if (filePath == null) {
            try {
                CoroutineManager.parseRemote(categoryName, ruleName);
            } catch (Exception e) {
                LOG.error("Parse remote sub rule failed, categoryName={}, ruleName={}", categoryName, ruleName, e);
            }
        } else {
            try {
                CoroutineManager.parseLocal(categoryName, ruleName, filePath);
            } catch (Exception e) {
                LOG.error("Parse local sub rule failed, categoryName={}, ruleName={}, filePath{}", categoryName, ruleName, filePath, e);
            }
        }

        Attribute timeoutAttribute = element.attribute(CoroutineConstants.TIMEOUT_ATTRIBUTE_NAME);
        if (timeoutAttribute != null) {
            long timeout = Long.parseLong(timeoutAttribute.getData().toString().trim());
            dependencyEntity.setTimeout(timeout);
        }
    }

    @SuppressWarnings("rawtypes")
    private void parseChain(Element element, ChainEntity chainEntity) {
        Attribute chainNameAttribute = element.attribute(CoroutineConstants.NAME_ATTRIBUTE_NAME);
        if (chainNameAttribute != null) {
            String chainName = chainNameAttribute.getData().toString().trim();
            chainEntity.setName(chainName);
        }

        List<StepEntity> stepEntityList = new ArrayList<StepEntity>();
        chainEntity.setStepEntityList(stepEntityList);

        for (Iterator elementIterator = element.elementIterator(); elementIterator.hasNext();) {
            Object childElementObject = elementIterator.next();
            if (childElementObject instanceof Element) {
                Element childElement = (Element) childElementObject;

                StepEntity stepEntity = new StepEntity();
                stepEntityList.add(stepEntity);

                parseStep(childElement, stepEntity);
            }
        }
    }

    private void parseStep(Element element, StepEntity stepEntity) {
        String indexValue = element.attribute(CoroutineConstants.INDEX_ATTRIBUTE_NAME).getData().toString().trim();
        String[] array = StringUtils.split(indexValue, ",");
        int[] index = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            index[i] = Integer.parseInt(array[i].trim());
        }
        stepEntity.setIndexes(index);

        if (StringUtils.equals(element.getName().trim(), StepType.THEN.toString())) {
            stepEntity.setStepType(StepType.THEN);
        } else if (StringUtils.equals(element.getName().trim(), StepType.WHEN.toString())) {
            stepEntity.setStepType(StepType.WHEN);
        }
    }
}