/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.stillingar.core.snapshot;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.ConfigurationService;
import org.brekka.stillingar.core.FallbackConfigurationSource;
import org.brekka.stillingar.core.delta.DeltaConfigurationService;

/**
 * Snapshot based implementation of {@link ConfigurationService} which provides atomic updates to group
 * definitions and adhoc updates to standalone values.
 * 
 * Implements {@link ConfigurationService} to support refreshing the configuration based new snapshots obtained
 * via the {@link SnapshotManager}. During a reload, standalone values will be updated first, then the groups. All will
 * be performed in the order they were registered.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class SnapshotBasedConfigurationService extends DeltaConfigurationService {
	
    /**
     * Where all snapshots will be obtained from
     */
	private final SnapshotManager snapshotManager;
	
	/**
	 * Handle errors
	 */
	private final SnapshotEventHandler snapshotEventHandler;
	
	/**
	 * Is an initial snapshot required? Should be set to true if the application
	 * cannot just rely on the defaults to load correctly.
	 */
	private final boolean initialSnapshotRequired;
	
	/**
	 * The current snapshot, used for error reporting.
	 */
	private Snapshot currentSnapshot;

	
    /**
     * Will require that an initial snapshot be present and will use the {@link ConsoleSnapshotEventHandler} for events.
     * 
     * @param snapshotManager
     *            manages the loading, validation of snapshots
     * @param defaultConfigurationSource
     *            the source of default configuration values (can be set to null).
     */
	public SnapshotBasedConfigurationService(SnapshotManager snapshotManager, 
	                                             ConfigurationSource defaultConfigurationSource) {
	    this (snapshotManager, false, defaultConfigurationSource, null);
	}
	
	/**
     * @param snapshotManager
     *            manages the loading, validation of snapshots
     * @param initialSnapshotRequired
     *            determines whether the system can continue on defaults only should there be no snapshot resource
     *            available.
     * @param defaultConfigurationSource the source of default configuration values (can be set to null).
     * @param snapshotEventHandler
     *            the handler to use for events
     */
	public SnapshotBasedConfigurationService(SnapshotManager snapshotManager,
	                                             boolean initialSnapshotRequired,
	                                             ConfigurationSource defaultConfigurationSource,
	                                             SnapshotEventHandler snapshotEventHandler) {
	    super(defaultConfigurationSource);
		this.snapshotManager = snapshotManager;
		this.initialSnapshotRequired = initialSnapshotRequired;
		this.snapshotEventHandler = (snapshotEventHandler != null 
		        ? snapshotEventHandler : new ConsoleSnapshotEventHandler());
		init();
	}
	
	
	/**
	 * Initialise this source based on the initial snapshot returned by the manager. If no initial snapshot is
	 * available and there is no default source, an exception will be thrown.
	 */
	protected void init() {
	    Snapshot initial = null;
	    try {
            initial = snapshotManager.retrieveInitial();
            currentSnapshot = initial;
        } catch (NoSnapshotAvailableException e) {
            boolean defaultsAvailable = getDelegate().getSecondarySource() != FallbackConfigurationSource.NONE;
            snapshotEventHandler.noInitialSnapshot(e, defaultsAvailable);
            if (!defaultsAvailable) {
                throw new ConfigurationException("No configuration available. See logs for details.");
            }
            if (initialSnapshotRequired) {
                throw new ConfigurationException("Could not find a configuration file with which to load the application. " +
                		" See the application logs for details of the locations being searched for configuration.");
            }
        }
	    
	    // Refresh using the initial, if it is available
	    ConfigurationSource initialSource = null;
	    if (initial != null) {
	        initialSource = initial.getSource();
	    } 
	    try {
	        refresh(initialSource);
	        snapshotEventHandler.initialConfigure(initial, null);
	    } catch (ChangeConfigurationException e) {
	        snapshotManager.reject(initial);
	        snapshotEventHandler.initialConfigure(initial, e);
	        throw new ConfigurationException("Failed to prepare initial configuration", e);
	    }
	    
	    // Failsafe - We must have at least one configuration source so make sure of it
	    if (getDelegate().getPrimarySource() == FallbackConfigurationSource.NONE 
	            && getDelegate().getSecondarySource() == FallbackConfigurationSource.NONE) {
	        throw new ConfigurationException("No default or custom configuration sources could be located.");
	    }
	}
	
    /**
     * Request that the configuration be updated to a new snapshot
     */
    public void refresh() {
        Snapshot updated = null;
        try {
            updated = snapshotManager.retrieveUpdated();
        } catch (InvalidSnapshotException e) {
            // Not fatal, notify with an event and move on
            snapshotEventHandler.invalidSnapshotUpdate(e);
        }
        if (updated != null) {
            // Configuration has changed, trigger a refresh
            try {
                refresh(updated.getSource());
                snapshotEventHandler.refreshConfigure(updated, null);
                this.currentSnapshot = updated;
            } catch (ChangeConfigurationException e) {
                snapshotManager.reject(updated);
                snapshotEventHandler.refreshConfigure(updated, e);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (currentSnapshot != null) {
            return String.format("%s[primary: in '%s', modified '%TF %<TT']", 
                    getClass().getSimpleName(), currentSnapshot.getLocation(), currentSnapshot.getTimestamp());
        }
        if (getDelegate() != null 
                && getDelegate().getSecondarySource() != null) {
            FallbackConfigurationSource delegated = getDelegate();
            ConfigurationSource defaults = delegated.getSecondarySource();
            return String.format("%s[fallback: %s]", getClass().getSimpleName(), defaults);
        }
        return String.format("%s[unknown]", getClass().getSimpleName());
    }
}
