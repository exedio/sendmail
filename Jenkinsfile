
timestamps
{
	def jdk = 'openjdk-8'
	def isRelease = env.BRANCH_NAME.toString().equals("master")

	properties([
			buildDiscarder(logRotator(
					numToKeepStr         : isRelease ? '1000' : '30',
					artifactNumToKeepStr : isRelease ? '1000' :  '2'
			))
	])

	//noinspection GroovyAssignabilityCheck
	lock('sendmail-remote') { node('docker')
	{
		try
		{
			abortable
			{
				echo("Delete working dir before build")
				deleteDir()

				def buildTag = makeBuildTag(checkout(scm))

				def dockerName = env.JOB_NAME.replace("/", "-") + "-" + env.BUILD_NUMBER
				def dockerDate = new Date().format("yyyyMMdd")
				def mainImage = docker.build(
						'exedio-jenkins:' + dockerName + '-' + dockerDate,
						'--build-arg JDK=' + jdk + ' ' +
						'conf/main')
				mainImage.inside(
						"--name '" + dockerName + "' " +
						"--cap-drop all " +
						"--security-opt no-new-privileges " +
						"--network none")
				{
					sh "java -version"
					sh "ant -version"

					sh "ant -noinput clean jenkins" +
							' "-Dbuild.revision=${BUILD_NUMBER}"' +
							' "-Dbuild.tag=' + buildTag + '"' +
							' -Dbuild.status=' + (isRelease?'release':'integration') +
							' -DtestRemote=false' +
							' -Dfindbugs.output=xml'
				}

				recordIssues(
						failOnError: true,
						enabledForFailure: true,
						ignoreFailedBuilds: false,
						qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]],
						tools: [
							java(),
							spotBugs(pattern: 'build/findbugs.xml', useRankAsPriority: true),
						],
				)
				mainImage.inside(
						"--name '" + dockerName + "' " +
						"--cap-drop all " +
						"--security-opt no-new-privileges")
						// TODO restrict network access to the smtp host in sendmail-remote.properties only
				{
					withCredentials([file(credentialsId: 'sendmail-remote.properties', variable: 'PROPERTIES')])
					{
						sh "ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-plain    -Dsmtp.port=25"
						sh "ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-ssltls   -Dsmtp.port=465 -Dsmtp.ssl=true"
						sh "ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-starttls -Dsmtp.port=587 -Dsmtp.enableStarttls=true"
						sh "ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-start25  -Dsmtp.port=25  -Dsmtp.enableStarttls=true"
						sh "ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-dsn	     -Dsmtp.port=25  -Dsmtp.returnPath.set=true -Dsmtp.dsn.notifySuccess=true"
					}
				}
				archiveArtifacts 'build/success/*'
			}
		}
		catch(Exception e)
		{
			//todo handle script returned exit code 143
			throw e
		}
		finally
		{
			// because junit failure aborts ant
			junit(
					allowEmptyResults: false,
					testResults: 'build/testresults/**/*.xml',
			)
			def to = emailextrecipients([culprits(), requestor()])
			//TODO details
			step([$class: 'Mailer',
					recipients: to,
					attachLog: true,
					notifyEveryUnstableBuild: true])

			echo("Delete working dir after build")
			deleteDir()
		}
	}}
}

def abortable(Closure body)
{
	try
	{
		body.call()
	}
	catch(hudson.AbortException e)
	{
		if(e.getMessage().contains("exit code 143"))
			return
		throw e
	}
}

def makeBuildTag(scmResult)
{
	return 'build ' +
			env.BRANCH_NAME + ' ' +
			env.BUILD_NUMBER + ' ' +
			new Date().format("yyyy-MM-dd") + ' ' +
			scmResult.GIT_COMMIT + ' ' +
			sh (script: "git cat-file -p " + scmResult.GIT_COMMIT + " | grep '^tree ' | sed -e 's/^tree //'", returnStdout: true).trim()
}
