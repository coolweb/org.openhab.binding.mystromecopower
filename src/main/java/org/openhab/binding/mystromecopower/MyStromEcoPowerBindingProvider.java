/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystromecopower;

import org.openhab.core.binding.BindingProvider;

/**
 * @author Jordens Christophe
 * @since 1.7.0-SNAPSHOT
 */
public interface MyStromEcoPowerBindingProvider extends BindingProvider {
	public String getMystromFriendlyName(String itemName);
	
	public Boolean getIsSwitch(String itemName);
	
	public Boolean getIsNumberItem(String itemName);
	
	public Boolean getIsStringItem(String itemName);
}
