# Change-Log

## 1.5 / YYYY-MM-DD

* Bump `maven-jxr-plugin` from 3.3.1 to 3.3.2
* Bump `checkstyle` from 10.12.6 to 10.12.7
* Bump `maven-jxr-plugin` from 3.2.0 to 3.3.1
* Bump `maven-javadoc-plugin` from 3.4.0 to 3.6.3
* Bump `maven-compiler-plugin` from 3.11.0 to 3.12.1
* Bump `maven-jar-plugin` from 3.2.2 to 3.3.0
* Bump `maven-install-plugin` from 3.0.0-M1 to 3.1.1
* Bump `maven-enforcer-plugin` from 3.1.0 to 3.4.1
* Bump `maven-deploy-plugin` from 3.0.0-M2 to 3.1.1
* Bump `maven-dependency-plugin` from 3.3.0 to 3.6.1
* Bump `maven-compiler-plugin` from 3.10.1 to 3.11.0
* Bump `maven-clean-plugin` from 3.2.0 to 3.3.2
* Bump `checkstyle` from 9.3 to 10.12.6
* Bump `maven-checkstyle-plugin` from 3.1.2 to 3.3.1
* Bump `maven-gpg-plugin` from 3.0.1 to 3.1.0
* Bump `maven-assembly-plugin` from 3.4.1 to 3.6.0
* SpellFix in README
* Update year in copyright

## 1.4 / 2022-07-19

* Set Version to 1.4
* Downgrade `maven-site-plugin` from 4.0.0-M1 to 3.12.0 because MPLUGIN-403
* Lint markdown-files
* Bump `spotbugs-maven-plugin` from 4.7.0.0 to 4.7.1.0
* Bump `maven-surefire-plugin` from 3.0.0-M6 to 3.0.0-M7
* Bump `maven-release-plugin` from 3.0.0-M5 to 3.0.0-M6
* Bump `maven-pmd-plugin` from 3.16.0 to 3.17.0
* Bump `maven-enforcer-plugin` from 3.0.0 to 3.1.0
* Bump `maven-assembly-plugin` from 3.3.0 to 3.4.1
* Make `JavaCC` dependency optional
* Normalize all the line endings
* Correct `sourcepath` of `maven-javadoc-plugin`
* Correct some JavaDoc's
* Return `List` instead of `Iterator` in `Directive.getAttributes`
* Simplify `toString` in `Directives` and `Directive`
* Corr: remove detect-links at javadoc-plugin
* Correct maven-min-version in building-documentation
* Upgrade JavaCC generation to modern-style
* Bump `javacc` from 7.0.11 to 7.0.12
* Add source-version and detect-links to javadoc-plugin
* Move `maven-gpg-plugin`-plugin-management to release-profile
* Restore old API of tag-file
* Exclude `lifecycle-mapping` from plugin-management-report
* Add `maven-release-plugin` to easily perform a deployment
* Reformat POM
* Change distribution to central-repo
* Add new profile `release` to deploy to central-repo
* Correct property-name in `license-maven-plugin`
* Add locale-property to get current year
* Add lifecycle-mapping for eclipse
* Update copyright
* Add `license-maven-plugin` to update copyright
* Add encoding-property
* Add new profile `assembly` to generate assemblies
* Change group-id from `taglibrarydoc` to `io.github.weblegacy`
* Suppress timestamp at javadoc-files
* Correction workaround MJAVADOC-700
* Update organization in POM
* Workaround MJAVADOC-700
* Reformationg xsl-files
* Correct keyword-output at generated html-pages
* No normalize-space at generated html (exception html-title)
* Update `changes.xml`
* No `pre`-tag for description
* Define sort-rules for html-generation
* Set new home-url (from `ste-gr` to `weblegacy`)
* Config pom to distribute artifacts to `GitHub packages`
* Add `maven-gpg-plugin` to sign artifacts
* Change output-directory of assembly-plugin
* Correct some SpotBugs
* Add `changes.xml` checks
* Doc: Use skin `maven-fluido-skin`
* Doc: Add logo to welcome-page
* Doc: Rename Screenshot-image
* Correct parameters for github-report
* Ignore missing Javadoc comments or tags during javadoc-generation
* Add dependency-plugin
* Add checkstyle-plugin
* Add spotbugs-plugin
* Exclude generated classes form PMD-report
* Move generated classes to own package
* New Class `Directives` to remove compiler-waring in generated code
* push new site-documentation with `site-deploy`
* Set new home-url
* Remove double used template `info` at `tld1_1-tld1_2.xsl`
* Use HTTPS in xsi:schemaLocation
* Replace trademark-sign at FAQs
* Convert documentation from XDoc to Markdown
* Remove JavaDoc-warnings
* Remove warnings in xdoc-files
* Bump `maven-site-plugin` from 3.11.0 to 4.0.0-M1
* Bump `maven-project-info-reports-plugin` from 3.2.2 to 3.3.0
* Bump `maven-javadoc-plugin` from 3.3.2 to 3.4.0
* Remove `PMD`-Warnings
* Update site-documentation
* Use of `maven-changes-plugin` and remove old `revision`-site
* Remove unnecessary dependency `junit`
* avoid duplicate generation of Source and JavaDoc
* Correction mem-leak in TagLibrary-implementation-classes
* Reformating: Remove eof-empty-lines, end-spaces, tabs to spaces
* Add scm-infos to pom
* Improvement at `TLDDocGenerator`
* Improvement at `tag-file.jjt`
* Improvements at JavaDoc
* Compile with JJTree/JavaCC
* Upgrade JDK 8: Generics, JavaDoc, try-with-resources, switch-with-strings, ...
* Set new URL and add issue-management
* Set License-URL and new License-Name
* Add new maintainer
* Set Version to 1.4-SNAPSHOT

## 1.3 / 2022-04-07

* Add CHANGELOG to assemblies
* Update site-documentation
* Source from TLDDoc 1.3 without tests

## 1.2 / 2022-04-07

* Add site-documentation (reconstructed)
* Add developer and contributor to POM
* Reconstructed Source from TLDDoc 1.2 without tests

## 1.1 / 2022-04-07

* Full updated POM
* Reconstructed Source from TLDDoc 1.1 without tests
* Add CHANGELOG.md
* Add LICENSE
* Add README.md
