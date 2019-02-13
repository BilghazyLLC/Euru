package io.euruapp.core.injection;

import dagger.Component;
import io.euruapp.core.BaseActivity;
import io.euruapp.core.BaseFragment;
import io.euruapp.core.EuruApplication;


/**
 * Dagger {@link Component} for injection
 */
@Component(modules = {ContextModule.class})
@AppScope
public interface AppComponent {
	
	//Injection point for the application level
	void inject(EuruApplication application);
	
	void inject(BaseActivity host);
	
	void inject(BaseFragment host);
	
}
