package nand.jack_analyzer.process;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.util.ArrayList;

import static nand.jack_analyzer.process.KeywordEnum.*;

public class CompilationEngine {
    private ArrayList<Token> tokens;
    private int nextTokenIndex = 0;
    private Token thisToken;
    public Document doc;
    private boolean routineStarted;
    private Element routine;
    private Element subRoutineBody;

    public CompilationEngine(ArrayList<Token> tokens) throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.newDocument();
        this.tokens = tokens;
        nextToken();
    }

    private Token nextToken() {
        thisToken = tokens.get(nextTokenIndex);
        nextTokenIndex++;
        return thisToken;
    }

    public void compileAll() {
        try {
            while (nextTokenIndex < tokens.size()) {
                if (thisToken.getClass() == Keyword.class && ((Keyword) thisToken).type == K_CLASS) {
                    compileClass();
                }
                nextToken();
            }
        } catch (Exception e) {
            System.err.println("Error while parsing at line " + (thisToken == null ? "undefined" : thisToken.lineNumber));
            e.printStackTrace();
        }
    }

    public void compileClass() {
        Element classToCompile = doc.createElement("class");
        addKeyword(classToCompile, (Keyword) thisToken);
        addIdentifier(classToCompile, (Identifier) nextToken());
        addSymbol(classToCompile, (Symbol) nextToken());
        nextToken();
        compileClassVarDec(classToCompile);
        compileSubRoutine(classToCompile);

        doc.appendChild(classToCompile);
    }

    public void compileClassVarDec(Element parent) {
        while (thisToken.getClass() == Keyword.class && (((Keyword) thisToken).type == K_FIELD || ((Keyword) thisToken).type == K_STATIC)) {
            // Example: field int number;
            Element classVarDecToCompile = doc.createElement("classVarDec");
            // field
            addKeyword(classVarDecToCompile, (Keyword) thisToken);

            // int (or user defined type)
            if (nextToken().getClass() == Keyword.class) {
                addKeyword(classVarDecToCompile, ((Keyword) thisToken));
            } else {
                addIdentifier(classVarDecToCompile, (Identifier) thisToken);
            }

            // number
            addIdentifier(classVarDecToCompile, (Identifier) nextToken());
            nextToken();

            if (thisToken.getClass() == Symbol.class && ((Symbol) thisToken).symbolType == SymbolEnum.S_COMMA) {
                // multiple properties defined and delimited using comma
                addSymbol(classVarDecToCompile, (Symbol) thisToken);
                addIdentifier(classVarDecToCompile, ((Identifier) nextToken()));
                nextToken();
            }

            // ;
            addSymbol(classVarDecToCompile, (Symbol) thisToken);

            parent.appendChild(classVarDecToCompile);
            nextToken();
        }
    }

        public void compileSubRoutine(Element parent) {
            // Stops the recursive call when } reached
            if (thisToken.getClass() == Symbol.class && ((Symbol) thisToken).symbolType == SymbolEnum.S_RIGHTBRACKET) {
                return;
            }

    //        nextToken();
            if (thisToken.getClass() == Keyword.class
                    && (((Keyword) thisToken).type == K_FUNCTION
                    || ((Keyword) thisToken).type == K_CONSTRUCTOR
                    || ((Keyword) thisToken).type == K_METHOD)
            ) {
                routine = doc.createElement("subroutineDec");

                addKeyword(routine, ((Keyword) thisToken));
                nextToken();
            }


            if (thisToken.getClass() == Keyword.class) {
                addKeyword(routine, (Keyword) thisToken);
                nextToken();

            } else if (thisToken.getClass() == Identifier.class) {
                addIdentifier(routine, (Identifier) thisToken);
                nextToken();
            }

            if (thisToken.getClass() == Identifier.class) {
                addIdentifier(routine, (Identifier) thisToken);
                nextToken();
            }

            if (thisToken.getClass() == Symbol.class && ((Symbol) thisToken).symbolType == SymbolEnum.S_LEFTBRACET) {
                compileParameterList(routine);
                nextToken();
            }

            if (thisToken.getClass() == Symbol.class && ((Symbol) thisToken).symbolType == SymbolEnum.S_LEFTBRACKET) {
                subRoutineBody = doc.createElement("subroutineBody");
                addSymbol(subRoutineBody, (Symbol) thisToken);
                nextToken();
            }

            while(thisToken.getClass() == Keyword.class && ((Keyword) thisToken).type == K_VAR) {
                Element varDecs = doc.createElement("varDec");
                // declarations in functions
                nextToken();
                subRoutineBody.appendChild(varDecs);
            }

            Element statements = doc.createElement("statements");
            // Create statements here
            subRoutineBody.appendChild(statements);
            addSymbol(subRoutineBody, new Symbol('}'));
            routine.appendChild(subRoutineBody);
            parent.appendChild(routine);

        }

    public void compileParameterList(Element parent) {
        Element parametersList = doc.createElement("parameterList");
        addSymbol(parent, (Symbol) thisToken);
        nextToken();

        while (thisToken.getClass() != Symbol.class || ((Symbol) thisToken).symbolType != SymbolEnum.S_RIGHTBRACET) {
            if (thisToken.getClass() == Identifier.class) {
                addIdentifier(parametersList, (Identifier) thisToken);
                nextToken();

            } else if (thisToken.getClass() == Keyword.class) {
                addKeyword(parametersList, (Keyword) thisToken);
                nextToken();

            } else if (thisToken.getClass() == Symbol.class && ((Symbol) thisToken).symbolType == SymbolEnum.S_COMMA) {
                addSymbol(parametersList, (Symbol) thisToken);
                nextToken();
            }
        }

        parent.appendChild(parametersList);
        addSymbol(parent, (Symbol) thisToken);
    }

    public void addIdentifier(Element parent, Identifier identifier) {
        Element identifierElement = doc.createElement("identifier");
        identifierElement.appendChild(doc.createTextNode(identifier.name));
        parent.appendChild(identifierElement);
    }
    public void addKeyword(Element parent, Keyword keyword) {
        Element keywordElement = doc.createElement("keyword");
        keywordElement.appendChild(doc.createTextNode(keyword.named));
        parent.appendChild(keywordElement);
    }

    public void addSymbol(Element parent, Symbol symbol) {
        Element symbolElement = doc.createElement("symbol");
        symbolElement.appendChild(doc.createTextNode(String.valueOf(symbol.symbolType.label)));
        parent.appendChild(symbolElement);
    }
}
