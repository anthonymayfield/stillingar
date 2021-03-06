/*
 * Copyright 2013 the original author or authors.
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

package org.brekka.stillingar.spring.snapshot;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

/**
 * TODO Description of WatchedResourceMonitorTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class WatchedResourceMonitorTest {
    
    private WatchedResourceMonitor watchedResourceMonitor;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        watchedResourceMonitor = new WatchedResourceMonitor(250);
    }
    
    @After
    public void tearDown() throws Exception {
        watchedResourceMonitor.destroy();
    }

    /**
     * Test method for {@link org.brekka.stillingar.spring.snapshot.WatchedResourceMonitor#hasChanged()}.
     */
    @Test
    public void testHasChanged() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName());
        dir.mkdirs();
        File file = new File(dir, "config.xml");
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        
        watchedResourceMonitor.initialise(new FileSystemResource(file));
        assertFalse(watchedResourceMonitor.hasChanged());
        Thread.sleep(500);
        
        for (int i = 0; i < 3; i++) {
            fos.write("Test\n".getBytes());
            fos.flush();
            file.setLastModified(System.currentTimeMillis());
            Thread.sleep(500);
            assertTrue(watchedResourceMonitor.hasChanged());
            Thread.sleep(500);
            assertFalse(watchedResourceMonitor.hasChanged());
        }
        fos.close();
        file.delete();
        dir.delete();
    }

}
