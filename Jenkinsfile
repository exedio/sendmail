
timestamps
{
	def jdk = 'openjdk-8-deb9'
	def isRelease = env.BRANCH_NAME.toString().equals("master")

	properties([
			buildDiscarder(logRotator(
					numToKeepStr         : isRelease ? '1000' : '30',
					artifactNumToKeepStr : isRelease ? '1000' :  '2'
			))
	])

	//noinspection GroovyAssignabilityCheck
	lock('sendmail-remote') { node(jdk)
	{
		try
		{
			abortable
			{
				echo("Delete working dir before build")
				deleteDir()

				def buildTag = makeBuildTag(checkout(scm))

				env.JAVA_HOME = tool type: 'jdk', name: jdk
				env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
				def antHome = tool 'Ant version 1.9.3'

				sh "java -version"
				sh "${antHome}/bin/ant -version"

				sh "${antHome}/bin/ant -noinput clean jenkins" +
						' "-Dbuild.revision=${BUILD_NUMBER}"' +
						' "-Dbuild.tag=' + buildTag + '"' +
						' -Dbuild.status=' + (isRelease?'release':'integration') +
						' -DtestRemote=false' +
						' -Dfindbugs.output=xml'

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
				withCredentials([file(credentialsId: 'sendmail-remote.properties', variable: 'PROPERTIES')])
				{
					sh "${antHome}/bin/ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-plain    -Dsmtp.port=25"
					sh "${antHome}/bin/ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-ssltls   -Dsmtp.port=465 -Dsmtp.ssl=true"
					sh "${antHome}/bin/ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-starttls -Dsmtp.port=587 -Dsmtp.enableStarttls=true"
					sh "${antHome}/bin/ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-start25  -Dsmtp.port=25  -Dsmtp.enableStarttls=true"
					sh "${antHome}/bin/ant -noinput test -propertyfile " + PROPERTIES + " -DtestRemote=true -Dtest-taskname=junit-dsn	     -Dsmtp.port=25  -Dsmtp.returnPath.set=true -Dsmtp.dsn.notifySuccess=true"
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
