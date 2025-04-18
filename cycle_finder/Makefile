# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Makefile for building cycle_finder, a tool for finding reference cycles in a
# Java program. Mainly useful when translating to a language without garbage
# collection.
#
# Author: Keith Stanger

SOURCE_DIR = src/main
JAVA_SOURCE_DIR = $(SOURCE_DIR)/java
RESOURCE_DIR = $(SOURCE_DIR)/resources
J2OBJC_ROOT = ..
TRANSLATOR_SOURCE_DIR = $(J2OBJC_ROOT)/translator/$(JAVA_SOURCE_DIR)

include ../make/common.mk
include ../make/j2objc_deps.mk
include ../java_deps/jars.mk

BUILD_DIR = build_result
CLASS_DIR = $(BUILD_DIR)/classes
TEST_CLASS_DIR = $(BUILD_DIR)/test
TRANSLATOR_CLASS_DIR = $(J2OBJC_ROOT)/translator/$(CLASS_DIR)

JAVA_SOURCES = \
    com/google/devtools/cyclefinder/CycleFinder.java \
    com/google/devtools/cyclefinder/Edge.java \
    com/google/devtools/cyclefinder/GraphBuilder.java \
    com/google/devtools/cyclefinder/NameList.java \
    com/google/devtools/cyclefinder/NameUtil.java \
    com/google/devtools/cyclefinder/Options.java \
    com/google/devtools/cyclefinder/ReferenceGraph.java \
    com/google/devtools/cyclefinder/Tarjans.java

RESOURCES = \
    com/google/devtools/cyclefinder/CycleFinder.properties \
    com/google/devtools/j2objc/J2ObjC.properties \
    com/google/devtools/j2objc/JRE.mappings \
    com/google/devtools/j2objc/reserved_names.txt

DIST_DEPS = $(JSR305_JAR) j2objc_annotations.jar
INTERNAL_DEPS = $(GUAVA_JAR) $(PROCYON_JARS) $(SCENELIB_JAR) $(PLUME_UTIL_JAR) \
    $(JSPECIFY_JAR) $(ERROR_PRONE_ANNOTATIONS_JAR)
ifdef JAVA_8
INTERNAL_DEPS += $(JAVAC_JAR)
endif
JAR_DEPS_DIST = $(DIST_DEPS:%=$(DIST_JAR_DIR)/%) $(INTERNAL_DEPS:%=$(JAVA_DEPS_JAR_DIR)/%)
JAR_DEPS_PATH = $(subst $(eval) ,:,$(strip $(JAR_DEPS_DIST)))
JUNIT_JAR_DIST = $(DIST_JAR_DIR)/$(JUNIT_JAR)

MAIN_CLASS = com.google.devtools.cyclefinder.CycleFinder
MANIFEST = $(BUILD_DIR)/manifest.mf
JAR = $(BUILD_DIR)/cycle_finder.jar
JAR_DIST = $(DIST_JAR_DIR)/cycle_finder.jar

JAVA_SOURCES_FULL = $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/%)
RESOURCE_FILES = $(RESOURCES:%=$(CLASS_DIR)/%)

TEST_CLASSPATH = \
    $(CLASS_DIR):$(TEST_CLASS_DIR):$(JUNIT_JAR_DIST):$(TRANSLATOR_CLASS_DIR):$(ERROR_PRONE_ANNOTATIONS_JAR)

# Files in dependent jars that aren't needed in combined jar.
UNUSED_JAR_CONTENTS = \
  $(CLASS_DIR)/[A-Z]* \
  $(CLASS_DIR)/[ahp]* \
  $(CLASS_DIR)/about_files \
  $(CLASS_DIR)/ant_tasks \
  $(CLASS_DIR)/compiler* \
  $(CLASS_DIR)/hook* \
  $(CLASS_DIR)/jdt* \
  $(CLASS_DIR)/com/google/common/cache \
  $(CLASS_DIR)/com/google/common/eventbus \
  $(CLASS_DIR)/com/google/common/[nrx]* \
  $(CLASS_DIR)/com/google/common/util \
  $(CLASS_DIR)/com/google/third_party \
  $(CLASS_DIR)/com/strobel \
  $(CLASS_DIR)/com/sun/[cij]* \
  $(CLASS_DIR)/com/sun/jvmstat \
  $(CLASS_DIR)/com/sun/nio/file \
  $(CLASS_DIR)/com/sun/rmi \
  $(CLASS_DIR)/com/sun/tools/attach \
  $(CLASS_DIR)/com/sun/tools/classfile \
  $(CLASS_DIR)/com/sun/tools/corba \
  $(CLASS_DIR)/com/sun/tools/doclets \
  $(CLASS_DIR)/com/sun/tools/ex* \
  $(CLASS_DIR)/com/sun/tools/hat \
  $(CLASS_DIR)/com/sun/tools/internal \
  $(CLASS_DIR)/com/sun/tools/javadoc \
  $(CLASS_DIR)/com/sun/tools/javah \
  $(CLASS_DIR)/com/sun/tools/javap \
  $(CLASS_DIR)/com/sun/tools/jdeps \
  $(CLASS_DIR)/com/sun/tools/jdi \
  $(CLASS_DIR)/com/sun/tools/jstat \
  $(CLASS_DIR)/com/sun/tools/script \
  $(CLASS_DIR)/com/sun/tools/serialver \
  $(CLASS_DIR)/com/sun/xml \
  $(CLASS_DIR)/dalvik \
  $(CLASS_DIR)/org/relaxng \
  $(CLASS_DIR)/scenelib/annotations/io/classfile \
  $(CLASS_DIR)/scenelib/annotations/t* \
  $(CLASS_DIR)/sun/[ajrs]* \
  $(CLASS_DIR)/sun/tools/[ans]* \
  $(CLASS_DIR)/sun/tools/jar \
  $(CLASS_DIR)/sun/tools/j[cimps]* \
  $(CLASS_DIR)/sun/tools/tree \

ifeq ($(shell uname), Linux)
PRUNE_CMD = bash -O globasciiranges -c "rm -rf $(UNUSED_JAR_CONTENTS)"
else
PRUNE_CMD = rm -rf $(UNUSED_JAR_CONTENTS)
endif

default: $(JAR)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(JAR_DIST) $(DIST_DIR)/cycle_finder

$(JAR): $(MANIFEST) $(RESOURCE_FILES) $(JAVA_SOURCES_FULL) \
  | $(CLASS_DIR) java_deps_dist annotations_dist translator
	@echo building cycle_finder jar
	@$(JAVAC) -sourcepath $(JAVA_SOURCE_DIR):$(TRANSLATOR_SOURCE_DIR) \
	    -classpath $(JAR_DEPS_PATH) -d $(CLASS_DIR) \
	    $(TRANSLATOR_BUILD_FLAGS) $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/%)
	@for lib in $(JAR_DEPS_DIST); do unzip -oq $$lib -d $(CLASS_DIR); done
	@$(PRUNE_CMD)
	@jar cfm $@-combined $(MANIFEST) -C $(CLASS_DIR) .
	@$(JAVA) -jar $(JAVA_DEPS_JAR_DIR)/$(JARJAR_JAR) process jarjar.rules $@-combined $@

# Format manifest classpath with each jar on a separate line, to avoid
# maximum line length of 72 bytes in UTF-8 encoding.
# http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
$(MANIFEST): | $(BUILD_DIR)
	@echo creating $@
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class:" $(MAIN_CLASS) >> $@
	@echo "Version:" $(J2OBJC_VERSION) >> $@

$(CLASS_DIR)/%: $(RESOURCE_DIR)/%
	@mkdir -p $(@D)
	@cp -f $< $@

$(CLASS_DIR)/%: $(J2OBJC_ROOT)/translator/$(RESOURCE_DIR)/%
	@mkdir -p $(@D)
	cp -f $< $@

DIRS = $(BUILD_DIR) $(CLASS_DIR) $(TEST_CLASS_DIR) $(DIST_DIR) $(DIST_JAR_DIR)

$(sort $(DIRS)):
	@mkdir -p $@

$(DIST_DIR)/cycle_finder: $(SOURCE_DIR)/bin/cycle_finder.sh | $(DIST_DIR)
	install $< $@

$(JAR_DIST): $(JAR) | $(DIST_JAR_DIR)
	install -m 0644 $< $@

dist: $(JAR_DIST) $(DIST_DIR)/cycle_finder
	@:

test: compile-tests
	$(JAVA) -classpath $(TEST_CLASSPATH) $(J2OBJC_JAVA_FLAGS) \
        junit.textui.TestRunner com.google.devtools.cyclefinder.CycleFinderTest

compile-tests: $(JAR) | $(TEST_CLASS_DIR)
	$(JAVAC) -encoding UTF-8 -sourcepath src/test/java \
	    -classpath $(TEST_CLASSPATH) -d $(TEST_CLASS_DIR) \
	    src/test/java/com/google/devtools/cyclefinder/CycleFinderTest.java
