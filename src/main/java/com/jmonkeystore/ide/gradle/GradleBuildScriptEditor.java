package com.jmonkeystore.ide.gradle;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Lovett Li
 * @author James Khan
 */
public class GradleBuildScriptEditor {

    private List<ASTNode> nodes;
    private File file;
    private List<String> gradleFileContents;

    public GradleBuildScriptEditor(File inputfile ) throws MultipleCompilationErrorsException, IOException {
        this( IOUtils.toString( new FileInputStream( inputfile ), StandardCharsets.UTF_8) );
        this.file = inputfile;
    }

    public GradleBuildScriptEditor(String scriptContents ) throws MultipleCompilationErrorsException {
        AstBuilder builder = new AstBuilder();
        nodes = builder.buildFromString( scriptContents );
    }

    public FindDependenciesVisitor insertDependency( String dependency ) throws IOException {
        FindDependenciesVisitor visitor = new FindDependenciesVisitor();
        walkScript( visitor );
        gradleFileContents = Files.readAllLines( Paths.get( file.toURI() ) );

        if( visitor.getDependenceLineNum() == -1 ) {

            if( !dependency.startsWith( "\t" ) ) {
                dependency = "\t" + dependency;;
            }

            gradleFileContents.add( "" );
            gradleFileContents.add( "dependencies {" );
            gradleFileContents.add( dependency );
            gradleFileContents.add( "}" );
        }
        else {

            if( visitor.getColumnNum() != -1 ) {
                gradleFileContents = Files.readAllLines( Paths.get( file.toURI() ) );
                StringBuilder builder = new StringBuilder( gradleFileContents.get( visitor.getDependenceLineNum() - 1 ) );
                builder.insert( visitor.getColumnNum() - 2, "\n" + dependency + "\n" );
                String dep = builder.toString();

                // dep = dep.replace("\n", System.lineSeparator());

                /*
                if( CoreUtil.isWindows() )
                {
                    dep.replace( "\n", "\r\n" );
                }
                else if( CoreUtil.isMac() )
                {
                    dep.replace( "\n", "\r" );
                }
                 */

                gradleFileContents.remove( visitor.getDependenceLineNum() - 1 );
                gradleFileContents.add( visitor.getDependenceLineNum() - 1, dep );
            }
            else {
                gradleFileContents.add( visitor.getDependenceLineNum() - 1, dependency );
            }
        }

        return visitor;
    }

    public List<GradleDependency> getAllDependencies() {
        FindDependenciesVisitor visitor = new FindDependenciesVisitor();
        walkScript( visitor );

        return visitor.getDependencies();
    }

    public void walkScript( GroovyCodeVisitor visitor ) {
        for( ASTNode node : nodes ) {
            node.visit( visitor );
        }
    }

    public List<String> getGradleFileContents()
    {
        return gradleFileContents;
    }

}
