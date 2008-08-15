package ptolemy.actor.gt.controller;

import ptolemy.kernel.util.Configurable;

public interface ConfigurableEntity extends Configurable {

    public Configurer getConfigurer();

}
