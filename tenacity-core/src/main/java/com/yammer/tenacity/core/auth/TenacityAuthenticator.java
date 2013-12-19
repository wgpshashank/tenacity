package com.yammer.tenacity.core.auth;

import com.google.common.base.Optional;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public class TenacityAuthenticator<C, P> implements Authenticator<C, P> {
    public static <C, P> Authenticator<C, P> wrap(Authenticator<C, P> authenticator,
                                                  TenacityPropertyKey tenacityPropertyKey) {
        return new TenacityAuthenticator<>(authenticator, tenacityPropertyKey);
    }

    private final Authenticator<C, P> underlying;
    private final TenacityPropertyKey tenacityPropertyKey;

    private TenacityAuthenticator(Authenticator<C, P> underlying, TenacityPropertyKey tenacityPropertyKey) {
        this.underlying = underlying;
        this.tenacityPropertyKey = tenacityPropertyKey;
    }

    @Override
    public Optional<P> authenticate(C credentials) throws AuthenticationException {
        try {
            return new TenacityAuthenticate(credentials).execute();
        } catch (HystrixRuntimeException err) {
            throw new AuthenticationException(err);
        }
    }

    private class TenacityAuthenticate extends TenacityCommand<Optional<P>> {
        private final C credentials;

        private TenacityAuthenticate(C credentials) {
            super(tenacityPropertyKey);
            this.credentials = credentials;
        }

        @Override
        protected Optional<P> run() throws Exception {
            return underlying.authenticate(credentials);
        }
    }
}