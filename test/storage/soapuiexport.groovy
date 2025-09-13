
/*

Copyright 2023 CgSe Computergrafik und Softwareentwicklung GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


/*
TODOS: 
 - Edit the Testcase and TestSuite type, if required (see th import statements at the beginning). SoapUI and ReadyAPI use different classes.
 - Edit the variable supportedInterfaces
 - You define, what you want to export, you can export
 - TestCases, see TestcaseWrapper.export
 - TestSuites, see Testset.export
 - Projects, see Repository.export
*/

import com.eviware.soapui.impl.wsdl.teststeps.*
import groovy.json.*
import com.eviware.soapui.model.testsuite.*
//Depending on your test tool you might need to work with */
// - SoapUI com.eviware.soapui.model.testcase.WsdlTestCase soapuiTestcase
// - ReadyAPI com.eviware.soapui.impl.wsdl.WsdlTestCasePro soapuiTestcase

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase
//Readyapi: import com.eviware.soapui.impl.wsdl.WsdlTestCasePro
import com.eviware.soapui.model.testsuite.TestSuite;
//Readyapi: import ReadyAPI com.eviware.soapui.impl.wsdl.WsdlTestSuitePro 
import java.util.regex.*
//Declaration part

log.info "Please check if you work with SoapUI or ReadyApI, und import the wsdl.*Pro classes or the model.* classes"
log.info "check 'supportedInterfaces'"



class QualifiedName {
	 String localName
	 String namespace
}

def supportedInterfaces = [
	"Wsdl1ServiceBinding",
	"Wsdl2ServiceBinding"
]

class TeststepWrapper {
		class teststep {
		 }
		class TeststepType {
		 	TeststepWrapper.teststep soap 
		 }
		class Teststep {
	
			class SOAPRequest {
				String body
				String authenticationType
				String username
				String password
				String url
				String methodType
				boolean createXopInclude
			}
			class SOAPResponse {
				boolean resolveXOPinclude
			}
			boolean enabled
			String Name 
			int order
			String serviceDefinition
			QualifiedName soapoperationQName
			QualifiedName wsdlServiceBindingQName
			SOAPRequest soapRequest
			SOAPResponse soapResponse
		}
		public TeststepWrapper(com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep teststep, def interfaceUrl, int order, def logger) {
			def soapTeststepType = new TeststepWrapper.TeststepType(soap : new  TeststepWrapper.teststep())
			def soapTeststep = new TeststepWrapper.Teststep(enabled: !teststep.disabled, serviceDefinition : interfaceUrl, order: order, name : teststep.name)
			def content = teststep.testRequest.getRequestContent()
			soapTeststep.soapRequest = new TeststepWrapper.Teststep.SOAPRequest()
			soapTeststep.soapResponse = new TeststepWrapper.Teststep.SOAPResponse()
			
			if( teststep.testRequest.getSelectedAuthProfile() == "BASIC"){
				soapTeststep.soapRequest.authenticationType = "basicAuthentication"
				
				soapTeststep.soapRequest.username = teststep.testRequest.getUsername()
				soapTeststep.soapRequest.password = teststep.testRequest.getPassword()
			}
			else {
				
				soapTeststep.soapRequest.authenticationType= "none"
				
			}
			soapTeststep.soapRequest.url = teststep.getPropertyValue('endpoint')
			soapTeststep.soapRequest.methodType = "post"
			soapTeststep.soapRequest.createXopInclude =  teststep.testRequest.isMtomEnabled() ||  teststep.testRequest.isForceMtom()
			soapTeststep.soapResponse.resolveXOPinclude = teststep.testRequest.isExpandMtomResponseAttachments()
			def newContent = replacePropertiesWithVariables(content)
			soapTeststep.soapRequest.body = Base64.getEncoder().encodeToString(newContent.getBytes());
			def qName = teststep.getInterface().getBindingName()
			teststep.getInterface().getBindingName().getLocalPart()
			soapTeststep.wsdlServiceBindingQName = new QualifiedName(localName: qName.getLocalPart(),namespace: qName.getNamespaceURI())
			soapTeststep.soapoperationQName = new QualifiedName(localName:teststep.getOperationName(), namespace:qName.getNamespaceURI())
			this.type = soapTeststepType
			this.element = soapTeststep
		}
		String replacePropertiesWithVariables(String input){
			
			def body = input
			def matcher =  Pattern.compile('\\$\\{(?<element>#Project)(?<separator>#)(?<name>\\w+)(?<closeBracket>\\})').matcher(body)
			body =  matcher.replaceAll('\\$\\(Repository\\.${name}\\)') 
			matcher =  Pattern.compile('\\$\\{(?<element>#TestSuite)(?<separator>#)(?<name>\\w+)(?<closeBracket>\\})').matcher(body)
			body =  matcher.replaceAll('\\$\\(Testset\\.${name}\\)') 
			matcher =  Pattern.compile('\\$\\{(?<element>#TestCase)(?<separator>#)(?<name>\\w+)(?<closeBracket>\\})').matcher(body)
			body =  matcher.replaceAll('\\$\\(Testcase\\.${name}\\)') 
			matcher =  Pattern.compile('\\$\\{(?<element>\\w+)(?<separator>#)(?<name>\\w+)(?<closeBracket>\\})').matcher(body)
			return  matcher.replaceAll('\\$\\(Testcase\\.${element}_${name}\\)') 
		}
		TeststepType type 
		Teststep element

}

	

class Variable {
	String key
	String value
	String valuetypeString
}

 class TestcaseWrapper {
 	
 	class Testcase {
	String description
	int order
	boolean enabled
	String name
	ArrayList<TeststepWrapper> teststeps
	ArrayList<Variable> variables
}
	class TestcaseType {
		class testcase {
		 }
		TestcaseWrapper.TestcaseType.testcase testcase
	}
	TestcaseWrapper.TestcaseType type
	TestcaseWrapper.Testcase element
	
	//ReadyAPI WsdlTestCasePro
    public TestcaseWrapper(WsdlTestCase soapuiTestcase, int order, def logger, def supportedInterfaces) {
 	     this.type = new TestcaseWrapper.TestcaseType(testcase: new  TestcaseWrapper.TestcaseType.testcase())
		this.element = new TestcaseWrapper.Testcase()
		element.description = "import from SoapUI"
		element.order = order
		element.enabled = !soapuiTestcase.disabled
		element.name = soapuiTestcase.name
		element.teststeps  = new ArrayList<TeststepWrapper> ()
		element.variables  = new ArrayList<Variable> ()
		
		soapuiTestcase.testStepList.forEach { value ->
			if (value instanceof com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep && value.getInterface() != null)  {	 
				def definition = ((WsdlTestRequestStep)value).getInterface().getDefinition()
				def qName = value.getInterface().getBindingName()
				if (supportedInterfaces.contains(qName.getLocalPart())) {
					def index = soapuiTestcase.getTestStepIndexByName(value.name)
					 def exportteststep = new TeststepWrapper((WsdlTestRequestStep)value, definition, index, logger)
					 this.element.teststeps.add(exportteststep)
				}
				else {
					logger.warn "not supported for export" + qName
				}
			}
			else if (value instanceof com.eviware.soapui.impl.wsdl.teststeps.WsdlPropertiesTestStep){
				for (TestProperty prop : value.getPropertyList()) {
					element.variables.add(new Variable(key: value.name+"_"+prop.getName(), value : prop.getValue(), valuetypeString : "string"))
			}
			}
				
			
		}
		def list = soapuiTestcase.getPropertyList()
		for (TestProperty prop : list) {
			element.variables.add(new Variable(key: prop.getName(), value : prop.getValue(), valuetypeString : "string"))
		}
 	 }
 	 //ReadyApi WsdlTestCasePro
 	 static def export(ArrayList<WsdlTestCase> soapuiTestCases, def logger,def generator, def file, def supportedInterfaces){
	 	
	 	def list = new ArrayList<TestcaseWrapper>()
	 	for (def testCase : soapuiTestCases) {
	 		def index = testCase.testSuite.getIndexOfTestCase(testCase)
	 		def testcase = new TestcaseWrapper(testCase, index,logger, supportedInterfaces)
	 		list.add(testcase)
	 	}
	 	assert (file != null)
	 	def actual = generator.toJson(list)
			file.write(actual)
		}
}
class Testset {
	int order
	boolean enabled
	String name
	ArrayList<TestcaseWrapper> testcases
	ArrayList<Variable> variables
		
 public Testset(TestSuite soapuiTestSuite, int order, def logger, def supportedInterfaces) {
 		this.order = order
		this.enabled = !soapuiTestSuite.disabled
		this.name = soapuiTestSuite.name
		this.testcases  = new ArrayList<TestcaseWrapper> ()
		this.variables  = new ArrayList<Variable> ()
		for (def testcase : soapuiTestSuite.getTestCaseList()) {
			def index = soapuiTestSuite.getIndexOfTestCase(testcase)
			def exporttestcase = new TestcaseWrapper(testcase, index,logger,supportedInterfaces)
			this.testcases.add(exporttestcase)
		}
		
		def list = soapuiTestSuite.getPropertyList()
		for (TestProperty prop : list) {
			this.variables.add(new Variable(key: prop.getName(), value : prop.getValue(), valuetypeString : "string"))
		}
	 }
	
	//ReadyAPI WsdlTestSuitePro
	static def export(ArrayList<TestSuite> soapuiTestSuites, def logger,def generator, def file, def supportedInterfaces){
	 	
	 	def list = new ArrayList<Testset>()
	 	
	 	for (def testSuite : soapuiTestSuites) {
	 		def index = testSuite.project.getIndexOfTestSuite(testSuite)
	 		def testset = new Testset(testSuite, index,logger, supportedInterfaces)
	 		
	 		list.add(testset)
	 	}
	 	assert (file != null)
	 	def actual = generator.toJson(list)
			file.write(actual)
		}
}

class Repository {
	int order
	boolean enabled
	String name
	ArrayList<Testset> testsets
	ArrayList<Variable> variables
	 public Repository(com.eviware.soapui.impl.wsdl.WsdlProject project, int order, def logger,def supportedInterfaces) {
	 	this.order = order
		this.enabled = !project.disabled
		this.name = project.name
		this.testsets  = new ArrayList<Testset> ()
		this.variables  = new ArrayList<Variable> ()
		
		for (def testsuite : project.getTestSuiteList()) {
			def index = testsuite.project.getIndexOfTestSuite(testsuite)
			def exporttestset = new Testset(testsuite,index,logger, supportedInterfaces)
			this.testsets.add(exporttestset)
		}
		
		def list = project.getPropertyList()
		for (TestProperty prop : list) {
			this.variables.add(new Variable(key: prop.getName(), value : prop.getValue(), valuetypeString : "string"))
		}
	 }
	 static def export(ArrayList<com.eviware.soapui.impl.wsdl.WsdlProject> projects,def  logger,def generator, def file, def supportedInterfaces){
	 	int i = 1
	 	def projectList = new ArrayList<Repository>()
	 	for (def project : projects) {
	 		def repository = new Repository(project, i,logger, supportedInterfaces)
	 		projectList.add(repository)
	 	}
	 	assert (file != null)
	 	def actual = generator.toJson(projectList)
			file.write(actual)
		}
}


//Output 

 
def  separator = System.getProperty("file.separator")
def homedir = System.getProperty("user.home")
def testcasepath =  homedir + "" +  separator + "temp" + separator +  "Testcases.json"
def testsetpath =  homedir + "" +  separator + "temp" + separator +  "Testsets.json"
def repositorypath =  homedir + "" +  separator + "temp" + separator +  "Repository.json"

def generator = new JsonGenerator.Options()
		    .excludeNulls()
		    .dateFormat("yyyy-MM-dd'T'hh:mm:ss")
		    .build()

//Export a list of testcases
TestcaseWrapper.export(context.testCase.testSuite.getTestCaseList(),log, generator, new File(testcasepath), supportedInterfaces)

log.info "testcase path" + testcasepath
//Export a list of testsuites


Testset.export(context.testCase.testSuite.project.getTestSuiteList(),log, generator, new File(testsetpath), supportedInterfaces)
log.info "testset path" + testsetpath
//Export a list of projects
Repository.export([context.testCase.testSuite.project],log, generator, new File(repositorypath), supportedInterfaces)
log.info "Repository path" + repositorypath