package io.euruapp.core.injection;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.euruapp.core.EuruApplication;

/**
 * Dagger {@link Module}
 */
@Module
public class ContextModule {
	
	private EuruApplication application;
	
	public ContextModule(EuruApplication application) {
		this.application = application;
	}
	
	@AppScope
	@Provides
	Context provideContext() {
		return application.getApplicationContext();
	}
	
}
