/*
 * CI/CD pipeline for the API automation suite.
 *
 * Environment selection is a build parameter, never a code change: the framework reads
 * -Denv and loads config/config-<env>.properties, so the same commit runs against SIT,
 * UAT or the mock stub server without a single edit.
 *
 * Credentials come from the Jenkins credential store and are passed as system properties,
 * which the ConfigReader resolves ahead of any value committed to the repository.
 */
pipeline {

    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-21'
            args '-v $HOME/.m2:/root/.m2'
        }
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['sit', 'uat', 'mock', 'live'],
                description: 'Target environment — maps to config/config-<env>.properties')
        choice(name: 'SUITE', choices: ['testng', 'smoke', 'negative', 'cucumber'],
                description: 'TestNG suite XML to execute')
        string(name: 'TAGS', defaultValue: 'not @wip',
                description: 'Cucumber tag expression, used when SUITE=cucumber')
        string(name: 'THREADS', defaultValue: '4', description: 'Parallel threads')
        booleanParam(name: 'RUN_BDD_TOO', defaultValue: true,
                description: 'Run the BDD suite after the API suite')
    }

    options {
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        MAVEN_OPTS = '-XX:MaxRAMPercentage=75 -Djava.awt.headless=true'
        // Credentials are injected, never committed. Create these in Jenkins first.
        API_CREDENTIALS = credentials('api-automation-service-account')
    }

    triggers {
        // Nightly regression against UAT.
        cron('H 2 * * *')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    currentBuild.description = "env=${params.ENVIRONMENT} suite=${params.SUITE}"
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B -ntp clean test-compile'
            }
        }

        stage('API tests') {
            steps {
                sh """
                    mvn -B -ntp test \
                        -Denv=${params.ENVIRONMENT} \
                        -Dsuite=${params.SUITE} \
                        -Dthreads=${params.THREADS} \
                        -Dauth.username=\$API_CREDENTIALS_USR \
                        -Dauth.password=\$API_CREDENTIALS_PSW
                """
            }
        }

        stage('BDD tests') {
            when {
                allOf {
                    expression { params.RUN_BDD_TOO }
                    expression { params.SUITE != 'cucumber' }
                }
            }
            steps {
                sh """
                    mvn -B -ntp test \
                        -Denv=${params.ENVIRONMENT} \
                        -Dsuite=cucumber \
                        -Dtags="${params.TAGS}" \
                        -Dauth.username=\$API_CREDENTIALS_USR \
                        -Dauth.password=\$API_CREDENTIALS_PSW
                """
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true

            // Allure report (requires the Allure Jenkins plugin and a configured commandline tool).
            script {
                if (fileExists('target/allure-results')) {
                    allure includeProperties: false,
                           jdk: '',
                           results: [[path: 'target/allure-results']]
                }
            }

            publishHTML(target: [
                    reportDir           : 'target/cucumber-reports',
                    reportFiles         : 'cucumber.html',
                    reportName          : 'Cucumber report',
                    keepAll             : true,
                    alwaysLinkToLastBuild: true,
                    allowMissing        : true
            ])

            archiveArtifacts artifacts: 'target/logs/**, target/cucumber-reports/**, target/surefire-reports/**',
                    allowEmptyArchive: true, fingerprint: true
        }

        failure {
            echo "API suite failed on ${params.ENVIRONMENT}. Reports are attached to build ${env.BUILD_NUMBER}."
            // mail to: 'qa-team@example.com',
            //      subject: "API tests FAILED — ${params.ENVIRONMENT} — build ${env.BUILD_NUMBER}",
            //      body: "${env.BUILD_URL}"
        }

        cleanup {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}
