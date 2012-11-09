/*
 * Copyright 2011 the original author or authors.
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

package org.brekka.stillingar.core.conversion;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Andrew Taylor
 */
public class DateConverter extends AbstractTemporalConverter<Date> {

    public final Class<Date> targetType() {
        return Date.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.TypeConverter#convert(java.lang.Object)
     */
    @Override
    public Date convert(Object obj) {
        Date value;
        if (obj instanceof Date) {
            value = (Date) obj;
        } else if (obj instanceof Calendar) {
            Calendar cal = (Calendar) obj;
            value = cal.getTime();
        } else if (obj instanceof String) {
            value = parseString((String) obj);
        } else {
            value = super.convert(obj);
        }
        return value;
    }
}
