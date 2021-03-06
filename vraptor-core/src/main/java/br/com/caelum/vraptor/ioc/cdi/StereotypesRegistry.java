package br.com.caelum.vraptor.ioc.cdi;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Convert;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.controller.DefaultBeanClass;
import br.com.caelum.vraptor.core.ControllerQualifier;
import br.com.caelum.vraptor.core.ConvertQualifier;
import br.com.caelum.vraptor.core.DeserializesQualifier;
import br.com.caelum.vraptor.core.InterceptorStackHandlersCache;
import br.com.caelum.vraptor.core.InterceptsQualifier;
import br.com.caelum.vraptor.core.StereotypeInfo;
import br.com.caelum.vraptor.events.VRaptorInitialized;
import br.com.caelum.vraptor.ioc.ControllerHandler;
import br.com.caelum.vraptor.ioc.ConverterHandler;
import br.com.caelum.vraptor.ioc.InterceptorStereotypeHandler;
import br.com.caelum.vraptor.serialization.Deserializes;
import br.com.caelum.vraptor.serialization.DeserializesHandler;

import com.google.common.collect.ImmutableMap;

@Dependent
public class StereotypesRegistry {

	private static final Map<Class<?>, StereotypeInfo> STEREOTYPES_INFO;

	@Inject private BeanManager beanManager;
	@Inject private InterceptorStackHandlersCache interceptorsCache;

	public void configure(@Observes VRaptorInitialized event){
		for (Bean<?> bean : beanManager.getBeans(Object.class)) {
			Annotation qualifier = tryToFindAStereotypeQualifier(bean);
			if (qualifier != null) {
				beanManager.fireEvent(new DefaultBeanClass(bean.getBeanClass()), qualifier);
			}
		}
		interceptorsCache.init();
	}

	private Annotation tryToFindAStereotypeQualifier(Bean<?> bean) {
		for (Class<? extends Annotation> annotation : bean.getStereotypes()) {
			if (STEREOTYPES_INFO.containsKey(annotation)) {
				return STEREOTYPES_INFO.get(annotation).getStereotypeQualifier();
			}
		}
		return null;
	}

	static {
		STEREOTYPES_INFO = ImmutableMap.<Class<?>, StereotypeInfo>of(
			Controller.class,new StereotypeInfo(Controller.class,ControllerHandler.class,new AnnotationLiteral<ControllerQualifier>() {}),
			Convert.class,new StereotypeInfo(Convert.class,ConverterHandler.class,new AnnotationLiteral<ConvertQualifier>() {}),
			Deserializes.class,new StereotypeInfo(Deserializes.class,DeserializesHandler.class,new AnnotationLiteral<DeserializesQualifier>() {}),
			Intercepts.class,new StereotypeInfo(Intercepts.class,InterceptorStereotypeHandler.class,new AnnotationLiteral<InterceptsQualifier>(){})
		);
	}
}