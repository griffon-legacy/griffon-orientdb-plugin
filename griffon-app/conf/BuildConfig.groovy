griffon.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        griffonHome()
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        String orientVersion = '1.3.0'
        
        compile "com.orientechnologies:orient-commons:$orientVersion",
                "com.orientechnologies:orientdb-core:$orientVersion",
                "com.orientechnologies:orientdb-client:$orientVersion",
                "com.orientechnologies:orientdb-object:$orientVersion"
        build('org.eclipse.jdt:org.eclipse.jdt.core:3.6.0.v_A58') {
            export = false
        }
        String lombokIdea = '0.5'
        build("de.plushnikov.lombok-intellij-plugin:processor-api:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:processor-core:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-factory:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-api:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-9:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-10:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-11:$lombokIdea") {
            export = false
            transitive = false
        }
        String ideaVersion = '11.1.4'
        build("org.jetbrains.idea:idea-openapi:$ideaVersion",
              "org.jetbrains.idea:extensions:$ideaVersion",
              "org.jetbrains.idea:util:$ideaVersion",
              "org.jetbrains.idea:annotations:$ideaVersion") {
            export = false
        }
    }
}

log4j = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon',
          'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}
