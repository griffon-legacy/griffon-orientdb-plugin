database {
    username = 'guest'
    password = 'guest'
    type     = 'document' // accepted values are 'document', 'object'
}
environments {
    development {
        database {
            url = 'local:/orient/databases/@griffon.project.name@-dev'
        }
    }
    test {
        database {
            url = 'local:/orient/databases/@griffon.project.name@-test'
        }
    }
    production {
        database {
            url = 'local:/orient/databases/@griffon.project.name@-prod'
        }
    }
}
