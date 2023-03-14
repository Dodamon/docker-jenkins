pipeline {
  agent any
  stages {
    stage('clone git lab repository') {
      steps {
        git branch: 'release', credentialsId: 'gitlab', url: 'https://lab.ssafy.com/s08-bigdata-recom-sub2/S08P22A504.git'
      }
    }
    stage('client, server parallel build') {
      parallel {
        stage('staging client build') {
          steps {
            script {
              try {
                echo 'build client'
                sh 'docker build -f frontend/Dockerfile.staging -t nowgnas/osakak:stage-client .'
              } catch (e) {
                echo 'client build fail'
                mattermostSend(
                  color: "#DF2E38",
                  message: "[CLIENT BUILD FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
                )
              }
            }
            
          }
        }
        stage('staging server build') {
          steps {
            script {
              try {
                echo 'build server'
                sh 'docker build -f backend/Dockerfile.staging -t nowgnas/osakak:stage-server .'
              } catch (e) {
                echo 'server build fail'
                mattermostSend(
                  color: "#DF2E38",
                  message: "[SERVER BUILD FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
                )
              }
            }
          }
        }
      }
    }
    stage('stop staging server') {
      steps {
        script {
          try {
            sh 'docker rm -f staging-client staging-server'
          } catch(e) {
            echo 'no running staging server'
          }
        }
      }
    }
    stage('run staging server') {
      steps {
        script {
          try {
            echo 'start staging server'
            sh 'docker login -u nowgnas -p dltkddnjs!!'
            sh 'cd /home/ubuntu/staging && docker-compose up -d'
          } catch (e) {
            echo 'staging server run fail'
            mattermostSend(
              color: "#DF2E38",
              message: "[RUN STAGING SERVER FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
            )
          }
        }
      }
    }
    stage('create merge request to release') {
      steps {
        script {
          try {
            sh 'curl -X POST -H "PRIVATE-TOKEN: 22HMryj9spaoUeCQ5sjM" -H "Content-Type: application/json" -d \'{"id": "287656", "source_branch": "release", "target_branch": "main", "title": ":twisted_rightwards_arrows: create merge request release into main"}\' https://lab.ssafy.com/api/v4/projects/287656/merge_requests'
          } catch (e) {
            echo "create merge request fail"
            mattermostSend(
              color: "#DF2E38",
              message: "[CREATE MERGE REQUEST FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
            )
          }
        }
      }
    }
    stage('remove unused images') {
      steps {
        script {
          try {
            sh 'docker images -qf dangling=true | xargs -I{} docker rmi {}'
        } catch (e) {
            echo 'no unused images'
          }
        }
      }
    }
    stage('release jenkins job finish') {
      steps {
        mattermostSend(
          color: "good",
          message: "[RELEASE JENKINS JOB SUCCESS]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
        )
      }
    }
  }
}