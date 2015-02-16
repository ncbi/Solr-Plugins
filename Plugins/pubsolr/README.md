# Building

To build solr collection plugin run
```
    gradlew jar
```
Built pubsolr-x.x.x.jar can be found in build/libs sub-folder. Copy this file to libs folder of your collection in solr.

To build solr collection plugin including dependencies required for command-line-interface run
```
    gradlew cli
```

Built pubsolr-cli-x.x.x.jar can be found in build/libs sub-folder. Example of creating compounds.fsa using this jar:
```
    java -jar pubsolr-cli-x.x.x.jar -c -t compounds.txt -f compounds.fsa
```

To create eclipse project files run
```
    gradlew eclipse
```
	
To recreate eclipse project files run
```
    gradlew cleanEclipse eclipse
```

# Solr configuration
## Changes to `solrconfig.xml`
### Request handler for data import

```xml
  <requestHandler name="/dataimport" class="org.apache.solr.handler.dataimport.DataImportHandler">
	<lst name="defaults">
	  <str name="config">pm-data-config.xml</str>
	</lst>
  </requestHandler>
```

### Request handler for web application 

```xml
    <requestHandler name="/search" class="solr.SearchHandler">
      <lst name="defaults">
		<str name="echoParams">explicit</str>

		<!-- VelocityResponseWriter settings -->
		<str name="wt">velocity</str>
		<str name="v.template">search</str>
		<str name="v.layout">layout</str>
		<str name="title">PubSolr</str>
		
		<!-- Query settings -->
		<str name="defType">plain</str>
		<str name="df">text</str>
		<!-- Boost factor for keywords recognized by text_with_concepts field type (5 by default)-->
		<!-- <str name="f.text.keyword.boost">5</str> -->
		<!-- Boosting fields separated by space; format of each field: field_name~max_length_in_words^boost -->
		<!-- <str name="bf">author~5^0.6 journal~8^0.5</str> -->
		<str name="mm">100%</str>
		<str name="q.alt">*</str>
		<str name="rows">10</str>
		<str name="fl">*,score</str>
		
		<str name="mlt.fl">title</str>
		<int name="mlt.count">3</int>
		
		<!-- Highlighting defaults -->
		<str name="hl">true</str>
		<str name="hl.fl">title author journal abstract mesh</str>
		<str name="hl.preserveMulti">true</str>
		<str name="hl.encoder">html</str>
		<str name="f.author.hl.requireFieldMatch">true</str>
		<str name="f.author.hl.fragsize">0</str>
		<str name="f.journal.hl.requireFieldMatch">true</str>
		<str name="f.journal.hl.fragsize">0</str>
		<str name="f.title.hl.fragsize">0</str>
		<str name="f.abstract.hl.snippets">3</str>
		<str name="f.abstract.hl.fragsize">200</str>
		<str name="f.mesh.hl.snippets">3</str>
		<str name="f.mesh.hl.fragsize">200</str>
    </lst>
  </requestHandler>
```

### Added query parser 

```xml
  <queryParser name="plain" class="com.agi.pubsolr.search.PlainQParserPlugin"/>
```

## Changes to `schema.xml`
### Added fields

```xml
   <field name="text" type="text_with_concepts" indexed="true" stored="false" multiValued="true" omitPositions="true"/>
   <field name="title" type="text_with_concepts" indexed="false" stored="true" multiValued="true"/>
   <field name="abstract" type="text_with_concepts" indexed="false" stored="true" multiValued="false"/>
   <field name="mesh" type="text_with_concepts" indexed="false" stored="true" multiValued="true"/>

   <field name="author" type="author" indexed="true" stored="true" multiValued="true"/>
   <field name="author_text" type="text_with_concepts" indexed="false" stored="false" multiValued="true"/>
   <field name="journal" type="journal" indexed="true" stored="true" multiValued="true"/>
   
   <field name="filter" type="ignored"/>
   <field name="keyword" type="ignored"/>
   <field name="subh" type="ignored"/>
   <field name="major" type="ignored"/>
   <field name="edat" type="ignored"/>
   <field name="aid" type="ignored"/>
   <field name="so" type="ignored"/>
```

### Added copyField commands

```xml
   <copyField source="title" dest="text"/>
   <copyField source="abstract" dest="text"/>
   <copyField source="mesh" dest="text"/>
   <copyField source="author_text" dest="text"/>
   <copyField source="journal" dest="text"/>
```

### Added field types
```xml
    <fieldType name="text_with_concepts" class="solr.TextField">
      <analyzer>
        <tokenizer class="solr.StandardTokenizerFactory"/>
        <filter class="solr.LowerCaseFilterFactory"/>
        <!-- <filter class="solr.KStemFilterFactory"/> -->
        <filter class="com.agi.pubsolr.analysis.ConceptFilterFactory" dictionary="compounds.fsa"/>
        <filter class="solr.StopFilterFactory" words="stopwords.txt" />
      </analyzer>
    </fieldType>

    <fieldType name="author" class="solr.TextField" sortMissingLast="true" omitNorms="true" omitTermFreqAndPositions="true">
      <analyzer>
        <tokenizer class="com.agi.pubsolr.analysis.AuthorTokenizerFactory"/>
      </analyzer>
    </fieldType>
	
    <fieldType name="journal" class="solr.TextField" sortMissingLast="true" omitNorms="true" omitTermFreqAndPositions="true">
      <analyzer>
        <tokenizer class="solr.KeywordTokenizerFactory"/>
        <filter class="solr.LowerCaseFilterFactory"/>
        <filter class="solr.PatternReplaceFilterFactory" pattern="([^a-z])" replacement=" " replace="all"/>
        <!-- The TrimFilter removes any leading or trailing whitespace -->
        <filter class="solr.TrimFilterFactory" />
      </analyzer>
    </fieldType>
```