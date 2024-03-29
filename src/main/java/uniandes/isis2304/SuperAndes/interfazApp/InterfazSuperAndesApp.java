package uniandes.isis2304.SuperAndes.interfazApp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import javax.jdo.JDODataStoreException;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import uniandes.isis2304.SuperAndes.negocio.Proveedor;
import uniandes.isis2304.SuperAndes.negocio.SuperAndes;
import uniandes.isis2304.SuperAndes.negocio.VOBodega;
import uniandes.isis2304.SuperAndes.negocio.VOCarritoCompra;
import uniandes.isis2304.SuperAndes.negocio.VOCarritoCompraProducto;
import uniandes.isis2304.SuperAndes.negocio.VOEstante;
import uniandes.isis2304.SuperAndes.negocio.VOOrdenPedido;
import uniandes.isis2304.SuperAndes.negocio.VOOrdenPedidoProducto;
import uniandes.isis2304.SuperAndes.negocio.VOProducto;
import uniandes.isis2304.SuperAndes.negocio.VOPromocion;
import uniandes.isis2304.SuperAndes.negocio.VOPromocionProducto;
import uniandes.isis2304.SuperAndes.negocio.VOProveedor;
import uniandes.isis2304.SuperAndes.negocio.VOSucursal;
import uniandes.isis2304.SuperAndes.negocio.VOTipoProducto;
import uniandes.isis2304.SuperAndes.negocio.VOTipoUsuario;
import uniandes.isis2304.SuperAndes.negocio.VOUsuario;
import uniandes.isis2304.SuperAndes.negocio.VOVenta;

@SuppressWarnings("serial")
public class InterfazSuperAndesApp extends JFrame implements ActionListener {
	
	/* ****************************************************************
	 * 			Constantes
	 *****************************************************************/

	private static Logger log = Logger.getLogger(InterfazSuperAndesApp.class.getName());
	private static final String CONFIG_INTERFAZ = "./src/main/resources/config/interfaceConfigApp";
	private static final String CONFIG_TABLAS = "./src/main/resources/config/TablasBD.json";
	
	private static long id_CarritoCompra;
	private static long nDocumento;
	
	//Constantes Adicionar al Carrro
	private static ArrayList<String> prod = new ArrayList();
	private static ArrayList<Integer> cant = new ArrayList();
	private static ArrayList<Integer> pVentaH = new ArrayList();

	
	
	/* ****************************************************************
	 * 			Atributos
	 *****************************************************************/

    private JsonObject tableConfig;
    private SuperAndes superAndes;
    private boolean permitirIngreso;
    private long id_Sucursal_U;
    private long id_Cliente_U;
    private JsonObject guiConfig;
    private PanelDatos panelDatos;
    private JMenuBar menuBar;

    
	/* ****************************************************************
	 * 			Métodos
	 *****************************************************************/

    public InterfazSuperAndesApp() {
    	
    	permitirIngreso = false;

		String rol[] = {"Administrador", "Gerente General", "Gerente Sucursal", "Operador", "Cajero", "Cliente"};
		JComboBox combo = new JComboBox(rol);
		JOptionPane.showMessageDialog(null, combo, "Tipos", JOptionPane.QUESTION_MESSAGE);

		
		try {
			nDocumento = Integer.parseInt(JOptionPane.showInputDialog(this, "Ingrese su ID:", "Abrir sesión", JOptionPane.INFORMATION_MESSAGE));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String rolActual = rol[combo.getSelectedIndex()];

		String json = CONFIG_INTERFAZ;

		if (rolActual.equals(rol[0]))
		{
			json += "Administrador.json";
		}
		else if(rolActual.equals(rol[1]))
		{
			json += "GerenteGeneral.json";
		}
		else if(rolActual.equals(rol[2]))
		{			
			json += "GerenteSucursal.json";
		}
		else if(rolActual.equals(rol[3]))
		{
			json += "Operador.json";
		}
		else if(rolActual.equals(rol[4]))
		{
			json += "Cajero.json";
		}
		else if(rolActual.equals(rol[5]))
		{
			json += "Cliente.json";
		}
    	
        guiConfig = openConfig ("Interfaz", json);
        
        configurarFrame ();
        if (guiConfig != null) {
        	
     	   crearMenu( guiConfig.getAsJsonArray("menuBar") );
        }
        
        tableConfig = openConfig ("Tablas BD", CONFIG_TABLAS);
        superAndes = new SuperAndes (tableConfig);
        
        List aux;
        
        if(!rolActual.equals("Administrador") && !rolActual.equals("Gerente General") && !rolActual.equals("Cliente")) {
        	aux = superAndes.darIdSucursalUsuarioConDocumentoIdTipoUsuario(nDocumento, superAndes.darIdPorTipoUsuario(rolActual).getId());
        	id_Sucursal_U = Long.parseLong(aux.get(0).toString());
        } else {
        	aux = superAndes.darNombreUsuarioConDocumentoIdTipoUsuario(nDocumento, superAndes.darIdPorTipoUsuario(rolActual).getId());
        }

 		if(aux.size() != 0) {
 			permitirIngreso = true;
 		}
 		else {
 			JOptionPane.showMessageDialog(null, "El usuario con ID: " + nDocumento + " NO exite en la base de datos\n o el ID: " + nDocumento + " NO está asociado al rol: " + rolActual, "ERROR", JOptionPane.ERROR_MESSAGE);
 		}
 		
 		if(!rolActual.equals("Administrador") && !rolActual.equals("Gerente General") && !rolActual.equals("Cliente")) {
 			JOptionPane.showMessageDialog(null, superAndes.darSucursalPorId(id_Sucursal_U).getNombre(), "Accediendo a la Sucursal:", JOptionPane.INFORMATION_MESSAGE);
 		}
        
    	String path = guiConfig.get("bannerPath").getAsString();
        panelDatos = new PanelDatos ( );

        setLayout (new BorderLayout());
        add (new JLabel (new ImageIcon (path)), BorderLayout.NORTH );          
        add( panelDatos, BorderLayout.CENTER );        
    }
    
    
	/* ****************************************************************
	 * 			Métodos de configuración de la interfaz
	 *****************************************************************/

    private JsonObject openConfig (String tipo, String archConfig) {
    	
    	JsonObject config = null;
		try {
			Gson gson = new Gson();
			FileReader file = new FileReader (archConfig);
			JsonReader reader = new JsonReader ( file );
			config = gson.fromJson(reader, JsonObject.class);
			log.info ("Se encontró un archivo de configuración válido: " + tipo);
		} 
		catch (Exception e) {
			
			log.info ("NO se encontró un archivo de configuración válido");			
			JOptionPane.showMessageDialog(null, "No se encontró un archivo de configuración de interfaz válido: " + tipo, "SuperAndes App", JOptionPane.ERROR_MESSAGE);
		}	
        return config;
    }

    private void configurarFrame() {
    	
    	int alto = 0;
    	int ancho = 0;
    	String titulo = "";	
    	
    	if ( guiConfig == null ) {
    		
    		log.info ( "Se aplica configuración por defecto" );			
			titulo = "SuperAndes APP Default";
			alto = 300;
			ancho = 500;
    	}
    	else {
    		
			log.info ( "Se aplica configuración indicada en el archivo de configuración" );
    		titulo = guiConfig.get("title").getAsString();
			alto= guiConfig.get("frameH").getAsInt();
			ancho = guiConfig.get("frameW").getAsInt();
    	}
    	
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setLocation (50,50);
        setResizable( true );
        setBackground( Color.WHITE );
        setTitle( titulo );
		setSize ( ancho, alto);        
    }

    private void crearMenu(  JsonArray jsonMenu ) {    	

        menuBar = new JMenuBar();       
        for (JsonElement men : jsonMenu) {
        	
        	JsonObject jom = men.getAsJsonObject(); 

        	String menuTitle = jom.get("menuTitle").getAsString();        	
        	JsonArray opciones = jom.getAsJsonArray("options");
        	
        	JMenu menu = new JMenu( menuTitle);
        	
        	for (JsonElement op : opciones) {
        		
        		JsonObject jo = op.getAsJsonObject(); 
        		String lb =   jo.get("label").getAsString();
        		String event = jo.get("event").getAsString();
        		
        		JMenuItem mItem = new JMenuItem( lb );
        		mItem.addActionListener( this );
        		mItem.setActionCommand(event);
        		
        		menu.add(mItem);
        	}       
        	menuBar.add( menu );
        }        
        setJMenuBar ( menuBar );	
    }
    
    
    /* ****************************************************************
	 * 			CRUD de Producto
	 *****************************************************************/

    public void listarProducto() {
    	
    	try {
			List <VOProducto> lista = superAndes.darVOProductos();
			String resultado = "En listar Productos";
			resultado +=  "\n" + listarProductos (lista);
			panelDatos.actualizarInterfaz(resultado);
			resultado += "\n Operación terminada";
		} 
    	catch (Exception e) {
    		
    		String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    
	/* ****************************************************************
	 * 			CRUD de Proveedor
	 *****************************************************************/

    public void listarProveedor() {
    	
    	try {
			List <VOProveedor> lista = superAndes.darVOProveedores();
			String resultado = "En listar Bodega";
			resultado +=  "\n" + listarProveedores (lista);
			panelDatos.actualizarInterfaz(resultado);
			resultado += "\n Operación terminada";
		} 
    	catch (Exception e) {
    		
    		String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }

    public void eliminarProveedorPorId() {
    	
    	try {
    		String idProveedorStr = JOptionPane.showInputDialog (this, "Id del Proveedor?", "Borrar el Proveedor por Id", JOptionPane.QUESTION_MESSAGE);
    		if (idProveedorStr != null) {
    			
    			long idProveedor = Long.valueOf (idProveedorStr);
    			long tbEliminados = superAndes.eliminarBodegaPorId (idProveedor);

    			String resultado = "En eliminar Proveedor\n\n";
    			resultado += tbEliminados + " Proveedor eliminado\n";
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else {
    			
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) {
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    
	/* ****************************************************************
	 * 			CRUD de Tipo de Productos
	 *****************************************************************/

    public void listarTipoProducto() {
    	
    	try {
			List <VOTipoProducto> lista = superAndes.darVOTiposProductos();
			String resultado = "En listar Bodegas";
			resultado +=  "\n" + listarTiposProductos (lista);
			panelDatos.actualizarInterfaz(resultado);
			resultado += "\n Operación terminada";
		} 
    	catch (Exception e) {
    		
    		String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }

    
    /* ****************************************************************
	 * 			CRUD de Tipo de Usuario
	 *****************************************************************/

    public void listarTipoUsuario() {
    	
    	try {
			List <VOTipoUsuario> lista = superAndes.darVOTiposUsuario();
			String resultado = "En listar Tipos de Usuarios";
			resultado +=  "\n" + listarTiposUsuario (lista);
			panelDatos.actualizarInterfaz(resultado);
			resultado += "\n Operación terminada";
		} 
    	catch (Exception e) {
    		
    		String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    public void adicionarTipoUsuario( )
    {
    	try 
    	{
    		String nombreTipo = JOptionPane.showInputDialog (this, "Nombre del tipo de Usuario", "Adicionar tipo de Usuario", JOptionPane.QUESTION_MESSAGE);
    		if (nombreTipo != null)
    		{
        		VOTipoUsuario tb = superAndes.adicionarTipoUsuario (nombreTipo);
        		if (tb == null)
        		{
        			throw new Exception ("No se pudo crear un tipo de usuario con nombre: " + nombreTipo);
        		}
        		String resultado = "En adicionarTipoUsuario\n\n";
        		resultado += "Tipo de usuario adicionado exitosamente: " + tb;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    
    /* ****************************************************************
	 * 			CRUD de Sucursal
	 *****************************************************************/

    public void listarSucursal() {
    	
    	try {
			List <VOSucursal> lista = superAndes.darVOSucursales();
			String resultado = "En listar Sucursales";
			resultado +=  "\n" + listarSucursales (lista);
			panelDatos.actualizarInterfaz(resultado);
			resultado += "\n Operación terminada";
		} 
    	catch (Exception e) {
    		
    		String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    public void adicionarSucursal( )
    {
    	try 
    	{
    		String nombre = JOptionPane.showInputDialog (this, "Nombre de la Sucursal", "Adicionar Sucursal", JOptionPane.QUESTION_MESSAGE);
    		String pais = JOptionPane.showInputDialog (this, "País de la Sucursal", "Adicionar Sucursal", JOptionPane.QUESTION_MESSAGE);
    		String ciudad = JOptionPane.showInputDialog (this, "Ciudad de la Sucursal", "Adicionar Sucursal", JOptionPane.QUESTION_MESSAGE);
    		String direccion = JOptionPane.showInputDialog (this, "Dirección de la Sucursal", "Adicionar Sucursal", JOptionPane.QUESTION_MESSAGE);
    		if (nombre != null || pais != null || ciudad != null || direccion != null )
    		{
        		VOSucursal tb = superAndes.adicionarSucursal (nombre, pais, ciudad, direccion, 1);
        		if (tb == null)
        		{
        			throw new Exception ("No se pudo crear una sucursal con nombre: " + nombre);
        		}
        		String resultado = "En adicionarSucursal\n\n";
        		resultado += "Sucursal adicionada exitosamente: " + tb;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }

    
    /* ****************************************************************
	 * 			CRUD de Usuario
	 *****************************************************************/

    public void listarUsuario() {
    	
    	try {
			List <VOUsuario> lista = superAndes.darVOUsuarios();
			String resultado = "En listar Usuarios";
			resultado +=  "\n" + listarUsuarios (lista);
			panelDatos.actualizarInterfaz(resultado);
			resultado += "\n Operación terminada";
		} 
    	catch (Exception e) {
    		
    		String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }

    public void adicionarUsuario( )
    {
    	try 
    	{
        	Integer puntos = null;
    		Long id_Sucursal = null;
    		
    		String[] opcTipoDocumento = {"Cedula Ciudadania", "Cedula Extranjeria", "Tarjeta de Identificacion", "Pasaporte", "NIT"};
			JComboBox opcionesTipoDocumento = new JComboBox(opcTipoDocumento);
			JOptionPane.showMessageDialog(null, opcionesTipoDocumento, "Seleccione el tipo de Documentos", JOptionPane.QUESTION_MESSAGE);
			String tipoDocumento = opcTipoDocumento[opcionesTipoDocumento.getSelectedIndex()];
    		
    		long nDocumento = Long.parseLong(JOptionPane.showInputDialog (this, "Numero de Documento", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE));	
    		String nombre = JOptionPane.showInputDialog (this, "Nombre", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
			String correo = JOptionPane.showInputDialog (this, "Correo Electrónico", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
			String pais = JOptionPane.showInputDialog (this, "Pais de Residencia", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
			String ciudad = JOptionPane.showInputDialog (this, "Ciudad de Residencia", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
    		String direccion = JOptionPane.showInputDialog (this, "Dirección de Residencia", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
    		
    		List tiposUsuario = superAndes.darNombreTiposUsuario();
    		String[] opcTiposUsuario = new String[tiposUsuario.size()];
    		for(int i = 0; i < tiposUsuario.size(); i++) {
    			opcTiposUsuario[i] = tiposUsuario.get(i).toString();
    		}
			JComboBox opcionesTiposUsuario = new JComboBox(opcTiposUsuario);
			JOptionPane.showMessageDialog(null, opcionesTiposUsuario, "Seleccione el tipo de Usuario", JOptionPane.QUESTION_MESSAGE);
			long id_TipoUsuario = superAndes.darIdPorTipoUsuario(opcTiposUsuario[opcionesTiposUsuario.getSelectedIndex()]).getId();

			if(opcTiposUsuario[opcionesTiposUsuario.getSelectedIndex()].equals("Administrador") || opcTiposUsuario[opcionesTiposUsuario.getSelectedIndex()].equals("Gerente General") || opcTiposUsuario[opcionesTiposUsuario.getSelectedIndex()].equals("Cliente")) {
    			
    			if(opcTiposUsuario[opcionesTiposUsuario.getSelectedIndex()].equals("Cliente")) {
    				
    				puntos = 0; 
    			}
        		
    		} else {
	
				List sucursales = superAndes.darNombreSucursales();
	    		String[] opcSucursales = new String[sucursales.size()];
	    		for(int i = 0; i < sucursales.size(); i++) {
	    			opcSucursales[i] = sucursales.get(i).toString();
	    		}
				JComboBox opcionesSucursales = new JComboBox(opcSucursales);
				JOptionPane.showMessageDialog(null, opcionesSucursales, "Seleccione la Sucursales", JOptionPane.QUESTION_MESSAGE);
				id_Sucursal = superAndes.darIdPorSucursal(opcSucursales[opcionesSucursales.getSelectedIndex()]).getId();
    		}
    		
    		if (!Objects.isNull(nDocumento) && tipoDocumento != null && nombre != null && correo != null && pais != null && ciudad != null && direccion != null)
    		{
        		VOUsuario tb = superAndes.adicionarUsuario (nDocumento, tipoDocumento, nombre, correo, pais, ciudad, direccion, puntos, id_TipoUsuario, id_Sucursal);
        		if (tb == null)
        		{
        			throw new Exception ("No se pudo crear el Usuario con nombre: " + nombre);
        		}
        		String resultado = "En adicionarUsuario\n\n";
        		resultado += "Usuario adicionada exitosamente: " + tb;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    public void adicionarUsuarioCliente( )
    {
    	try 
    	{
    		String[] opcTipoDocumento = {"Cedula Ciudadania", "Cedula Extranjeria", "Tarjeta de Identificacion", "Pasaporte", "NIT"};
			JComboBox opcionesTipoDocumento = new JComboBox(opcTipoDocumento);
			JOptionPane.showMessageDialog(null, opcionesTipoDocumento, "Seleccione el tipo de Documentos", JOptionPane.QUESTION_MESSAGE);
			String tipoDocumento = opcTipoDocumento[opcionesTipoDocumento.getSelectedIndex()];
    		
    		long nDocumento = Long.parseLong(JOptionPane.showInputDialog (this, "Numero de Documento", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE));	
    		String nombre = JOptionPane.showInputDialog (this, "Nombre", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
			String correo = JOptionPane.showInputDialog (this, "Correo Electrónico", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
			String pais = JOptionPane.showInputDialog (this, "Pais de Residencia", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
			String ciudad = JOptionPane.showInputDialog (this, "Ciudad de Residencia", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
    		String direccion = JOptionPane.showInputDialog (this, "Dirección de Residencia", "Adicionar Usuario", JOptionPane.QUESTION_MESSAGE);
    		Integer puntos = 0;
			long id_TipoUsuario = superAndes.darIdPorTipoUsuario("Cliente").getId();
			Long id_Sucursal = null;

    		if (!Objects.isNull(nDocumento) && tipoDocumento != null && nombre != null && correo != null && pais != null && ciudad != null && direccion != null)
    		{
        		VOUsuario tb = superAndes.adicionarUsuario (nDocumento, tipoDocumento, nombre, correo, pais, ciudad, direccion, puntos, id_TipoUsuario, id_Sucursal);
        		if (tb == null)
        		{
        			throw new Exception ("No se pudo crear el Cliente con nombre: " + nombre);
        		}
        		String resultado = "En adicionar Cliente\n\n";
        		resultado += "Cliente adicionada exitosamente: " + tb;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    /* ****************************************************************
	 * 			CRUD de Bodega
	 *****************************************************************/
    
    public void adicionarBodega( )
    {
    	try 
    	{
    		int volMax = Integer.parseInt(JOptionPane.showInputDialog (this, "Volumen Maximo", "Adicionar Bodega", JOptionPane.QUESTION_MESSAGE));	
    		int pesoMax = Integer.parseInt(JOptionPane.showInputDialog (this, "Peso Maximo", "Adicionar Bodega", JOptionPane.QUESTION_MESSAGE));
    		long id_Sucursal = id_Sucursal_U;
    		
    		List tiposProdutos = superAndes.darNombreTiposProductos();
    		String[] opcTiposProdutos = new String[tiposProdutos.size()];
    		for(int i = 0; i < tiposProdutos.size(); i++) {
    			opcTiposProdutos[i] = tiposProdutos.get(i).toString();
    		}
			JComboBox opcionesTiposProdutos = new JComboBox(opcTiposProdutos);
			JOptionPane.showMessageDialog(null, opcionesTiposProdutos, "Seleccione el tipo de Producto", JOptionPane.QUESTION_MESSAGE);
			long id_TipoProducto = superAndes.darIdPorTipoProducto(opcTiposProdutos[opcionesTiposProdutos.getSelectedIndex()]).getId();
			String tipoAlmacen = superAndes.darIdPorTipoProducto(opcTiposProdutos[opcionesTiposProdutos.getSelectedIndex()]).getTipoAlmacen();
    		
    		if (!Objects.isNull(volMax) && !Objects.isNull(pesoMax) && tipoAlmacen != null && !Objects.isNull(id_Sucursal) && !Objects.isNull(id_TipoProducto))
    		{
        		VOBodega tb = superAndes.adicionarBodega(volMax, pesoMax, tipoAlmacen, id_Sucursal, id_TipoProducto);
        		if (tb == null)
        		{
        			throw new Exception ("No se pudo crear la Bodega");
        		}
        		String resultado = "En adicionar Bodega\n\n";
        		resultado += "Bodega adicionada exitosamente: " + tb;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    
    /* ****************************************************************
	 * 			CRUD de Estante
	 *****************************************************************/
    
    public void adicionarEstante( )
    {
    	try 
    	{
    		int volMax = Integer.parseInt(JOptionPane.showInputDialog (this, "Volumen Maximo", "Adicionar Estante", JOptionPane.QUESTION_MESSAGE));	
    		int pesoMax = Integer.parseInt(JOptionPane.showInputDialog (this, "Peso Maximo", "Adicionar Estante", JOptionPane.QUESTION_MESSAGE));
    		long id_Sucursal = id_Sucursal_U;
    		int nAbastecimiento = Integer.parseInt(JOptionPane.showInputDialog (this, "Numero Para Abastecimiento", "Adicionar Estante", JOptionPane.QUESTION_MESSAGE));
    		
    		List tiposProdutos = superAndes.darNombreTiposProductos();
    		String[] opcTiposProdutos = new String[tiposProdutos.size()];
    		for(int i = 0; i < tiposProdutos.size(); i++) {
    			opcTiposProdutos[i] = tiposProdutos.get(i).toString();
    		}
			JComboBox opcionesTiposProdutos = new JComboBox(opcTiposProdutos);
			JOptionPane.showMessageDialog(null, opcionesTiposProdutos, "Seleccione el tipo de Producto", JOptionPane.QUESTION_MESSAGE);
			long id_TipoProducto = superAndes.darIdPorTipoProducto(opcTiposProdutos[opcionesTiposProdutos.getSelectedIndex()]).getId();
			String tipoAlmacen = superAndes.darIdPorTipoProducto(opcTiposProdutos[opcionesTiposProdutos.getSelectedIndex()]).getTipoAlmacen();
    		
    		if (!Objects.isNull(volMax) && !Objects.isNull(pesoMax) && tipoAlmacen != null && !Objects.isNull(nAbastecimiento) && !Objects.isNull(id_Sucursal) && !Objects.isNull(id_TipoProducto))
    		{
        		VOEstante tb = superAndes.adicionarEstante(volMax, pesoMax, tipoAlmacen, nAbastecimiento, id_Sucursal, id_TipoProducto);
        		if (tb == null)
        		{
        			throw new Exception ("No se pudo crear el Estante");
        		}
        		String resultado = "En adicionar Estante\n\n";
        		resultado += "Estante adicionado exitosamente: " + tb;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    public void abastecerEstante( )
    {
    	try
    	{
    		List<Object> idEstantes = superAndes.darIdEstantes();
    		
    		String[] opcIdEstantes = new String[idEstantes.size()];
    		for(int i = 0; i < idEstantes.size(); i++) {
    			opcIdEstantes[i] = idEstantes.get(i).toString();
    		}
			JComboBox opcionesIdEstantes = new JComboBox(opcIdEstantes);
			JOptionPane.showMessageDialog(null, opcionesIdEstantes, "Seleccione el Estante para aprovisionar", JOptionPane.QUESTION_MESSAGE);
			long id_Estante = Long.parseLong(opcionesIdEstantes.getSelectedItem().toString());
			
			
			long id_TipoProducto = Long.parseLong(superAndes.darTipoProductoId(id_Estante).get(0).toString());
			int nAbastecimiento = Integer.parseInt(superAndes.darNAbastecimientoId(id_Estante).get(0).toString());
			System.out.println(id_TipoProducto);
			System.out.println(nAbastecimiento);
			
			List<Long> id_Productos = superAndes.darIdProductoPorTipoProducto(id_TipoProducto);
			List<Integer> sb_Productos = superAndes.darSBProductoPorTipoProducto(id_TipoProducto);
			List<Integer> se_Productos = superAndes.darSEProductoPorTipoProducto(id_TipoProducto);
			
			List<Integer> new_sb_Productos = new ArrayList<Integer>();
			List<Integer> new_se_Productos = new ArrayList<Integer>();
			
			for(int i = 0; i < id_Productos.size(); i++) {
				new_sb_Productos.add(sb_Productos.get(i).intValue());
				new_se_Productos.add(se_Productos.get(i).intValue());
			}
			
			System.out.println(id_Productos);
			System.out.println(new_sb_Productos);
			System.out.println(new_se_Productos);
			
			/*
			for(int i = 0; i < id_Productos.size(); i++) {

				for(int j = 0; j < 100; i++) {
				
					sb_Productos.set(i, Long.sum(sb_Productos.get(i), 1));
					sb_Productos.set(i, Long. (sb_Productos.get(i), 1));
					
					se_Productos.add(i, se_Productos.get(i).longValue() + 1);
					
					System.out.println(sb_Productos);
					System.out.println(se_Productos);
					
					wait(1000);
					
				}
				
			}
    		

			
			
/*
			for(int i = 0; i < producto.size(); i++) {
				
				System.out.println(producto.get(i).getNombre());
			}

    		*/
    		
    		/*
    		
    		List<Object> ordenPedidoProducto = superAndes.darProductoPorIdOrdenPedido(id_OrdenPedido);
    		List<Object> ordenPedidoCantCompra = superAndes.darCantCompraPorIdOrdenPedido(id_OrdenPedido);
    		System.out.println(ordenPedidoProducto.get(0).toString());
    		System.out.println(ordenPedidoCantCompra.get(0).toString());
    		
			if (ordenPedidoProducto != null && !Objects.isNull(id_OrdenPedido) ) {
				
				
				for(int i = 0; i < ordenPedidoProducto.size(); i++) {
					
					superAndes.cambiarStock(Integer.parseInt(ordenPedidoCantCompra.get(i).toString()), Long.parseLong(ordenPedidoProducto.get(i).toString()));
				}
				
				superAndes.cambiarEsatdo("despachado", id_OrdenPedido);

    			panelDatos.actualizarInterfaz("Se registro la llegada del Pedido y se actualizaron los inventarios");
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}*/
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    
    /* ****************************************************************
	 * 			CRUD de Proveedor
	 *****************************************************************/
    
    public void adicionarProveedor( )
    {
    	try 
    	{
    		long nit = Long.parseLong(JOptionPane.showInputDialog (this, "NIT", "Adicionar Proveedor", JOptionPane.QUESTION_MESSAGE));	
    		String nombre = JOptionPane.showInputDialog (this, "Nombre de la Empresa", "Adicionar Proveedor", JOptionPane.QUESTION_MESSAGE);
    		int calificacion = 0;
    		
    		if (!Objects.isNull(nit) && nombre != null)
    		{
        		VOProveedor tb = superAndes.adicionarProveedor(nit, nombre, calificacion);
        		if (tb == null)
        		{
        			throw new Exception ("No se pudo crear el Proveedor");
        		}
        		String resultado = "En adicionar Proveedor\n\n";
        		resultado += "Proveedor adicionado exitosamente: " + tb;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    
    
    /* ****************************************************************
	 * 			CRUD de Producto
	 *****************************************************************/
    
    public void adicionarProducto( )
    {
    	try 
    	{
    		String fVencimiento;
    		
    		String codigoBarra = JOptionPane.showInputDialog (this, "Codigo de Barra", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE);
    		String nombre = JOptionPane.showInputDialog (this, "Nombre del Producto", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE);
    		String marca = JOptionPane.showInputDialog (this, "Marca del Producto", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE);
    		int pVenta = Integer.parseInt(JOptionPane.showInputDialog (this, "Precio de Venta", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));	
    		String presentacion = JOptionPane.showInputDialog (this, "Presentacion del Prducto", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE);
    		int pUnidadMedida = Integer.parseInt(JOptionPane.showInputDialog (this, "Precio por Unidad de Medida", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));
    		int cantPPT = Integer.parseInt(JOptionPane.showInputDialog (this, "Cantidad en la Presentacion", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));
    		String unidadMedida = JOptionPane.showInputDialog (this, "Unidad de Medida", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE);
    		int espEmpPeso = Integer.parseInt(JOptionPane.showInputDialog (this, "Especificacion de Peso del Empacado", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));
    		int espEmpVol = Integer.parseInt(JOptionPane.showInputDialog (this, "Especificacion de Volumen del Empacado", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));
    		
    		String bool[] = {"true","false"};
			JComboBox opcionesBool = new JComboBox(bool);
			JOptionPane.showMessageDialog(this, opcionesBool, "Es Perecedero?", JOptionPane.QUESTION_MESSAGE);
			String esPerecedero = (String) opcionesBool.getSelectedItem();
    		
    		if(esPerecedero.equals("true")) {
    			fVencimiento = JOptionPane.showInputDialog (this, "Fecha Vencimiento (dd/MM/yyyy)", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE);
    		} else { fVencimiento = null; }
    		
    		int nReorden = Integer.parseInt(JOptionPane.showInputDialog (this, "Numero de Reordenamiento", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));
    		int stockBodega = Integer.parseInt(JOptionPane.showInputDialog (this, "Stock en Bodega", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));
    		int stockEstante = Integer.parseInt(JOptionPane.showInputDialog (this, "Stock en Estantes", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));
    		int stockTotal = Integer.parseInt(JOptionPane.showInputDialog (this, "Stock Total", "Adicionar Producto", JOptionPane.QUESTION_MESSAGE));
    		
    		List tiposProdutos = superAndes.darNombreTiposProductos();
    		String[] opcTiposProdutos = new String[tiposProdutos.size()];
    		for(int i = 0; i < tiposProdutos.size(); i++) {
    			opcTiposProdutos[i] = tiposProdutos.get(i).toString();
    		}
			JComboBox opcionesTiposProdutos = new JComboBox(opcTiposProdutos);
			JOptionPane.showMessageDialog(null, opcionesTiposProdutos, "Seleccione el tipo de Producto", JOptionPane.QUESTION_MESSAGE);
			long id_TipoProducto = superAndes.darIdPorTipoProducto(opcTiposProdutos[opcionesTiposProdutos.getSelectedIndex()]).getId();

    		if (codigoBarra != null && nombre != null && marca != null && !Objects.isNull(pVenta) && presentacion != null && 
    				!Objects.isNull(pUnidadMedida) && !Objects.isNull(cantPPT) && unidadMedida != null && !Objects.isNull(espEmpPeso) && 
    				!Objects.isNull(espEmpVol) && !Objects.isNull(esPerecedero) && !Objects.isNull(nReorden) && !Objects.isNull(stockBodega) && 
    				!Objects.isNull(stockEstante) && !Objects.isNull(stockTotal) && !Objects.isNull(id_TipoProducto)) {
    			
        		VOProducto tb = superAndes.adicionarProducto(codigoBarra, nombre, marca, pVenta, presentacion, pUnidadMedida, cantPPT, unidadMedida, espEmpPeso, espEmpVol, esPerecedero, fVencimiento, nReorden, stockBodega, stockEstante, stockTotal, id_TipoProducto);
        		if (tb == null)
        		{
        			throw new Exception ("No se pudo crear el Producto");
        		}
        		String resultado = "En adicionar Producto\n\n";
        		resultado += "Producto adicionado exitosamente: " + tb;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    
    /* ****************************************************************
	 * 			CRUD de Promocion
	 *****************************************************************/
    
    public void adicionarPromocion( )
    {
    	try
    	{
    		String tipoPromocion = "";
    		int lleve = 0;
    		int pague = 0;
    		float descuento = 0;
    		int pVenta = 0;
    		int cantProd = 1;
    		
    		String opcPromocion[] = {"Pague N lleve M Unidades", "Hacer Descuento a Producto", "Pague X lleve Y Cantidad", "Pague 1 y el Segundo con Descuento", "Paquete de Productos"};
			JComboBox opcionesPromocion = new JComboBox(opcPromocion);
			JOptionPane.showMessageDialog(this, opcionesPromocion, "Seleccione el tipo de Promocion", JOptionPane.QUESTION_MESSAGE);
			String tipoPromocionSelected = (String) opcionesPromocion.getSelectedItem();
			
			
			String nombre = JOptionPane.showInputDialog (this, "Nombre Promocion", "Adicionar Promocion", JOptionPane.QUESTION_MESSAGE);
			String fInicio = JOptionPane.showInputDialog (this, "Fecha Inicio (dd/MM/yyyy)", "Adicionar Promocion", JOptionPane.QUESTION_MESSAGE);
			String fFin = JOptionPane.showInputDialog (this, "Fecha Fin (dd/MM/yyyy)", "Adicionar Promocion", JOptionPane.QUESTION_MESSAGE);
			String descripcion = JOptionPane.showInputDialog (this, "Descripcion", "Adicionar Promocion", JOptionPane.QUESTION_MESSAGE);
			long id_Sucursal = id_Sucursal_U;
			
			if (tipoPromocionSelected.equals(opcPromocion[0])) //PagueNlleveM
			{
				tipoPromocion = "PagueNlleveM";
				lleve = Integer.parseInt(JOptionPane.showInputDialog (this, "Unidades que Lleva", "Pague N lleve M Unidades", JOptionPane.QUESTION_MESSAGE));
				pague = Integer.parseInt(JOptionPane.showInputDialog (this, "Unidades que Paga", "Pague N lleve M Unidades", JOptionPane.QUESTION_MESSAGE));
			}
			else if(tipoPromocionSelected.equals(opcPromocion[1])) //DescuentoPorcentaje
			{
				tipoPromocion = "DescuentoPorcentaje";
				descuento = Float.parseFloat(JOptionPane.showInputDialog (this, "Descuento en porcentaje que aplicara", "Hacer Descuento a Producto", JOptionPane.QUESTION_MESSAGE))/100;
			}
			else if(tipoPromocionSelected.equals(opcPromocion[2])) //PagueXlleveY
			{
				tipoPromocion = "PagueXlleveY";
				lleve = Integer.parseInt(JOptionPane.showInputDialog (this, "Cantidad que Lleva", "Pague X lleve Y Cantidad", JOptionPane.QUESTION_MESSAGE));
				pague = Integer.parseInt(JOptionPane.showInputDialog (this, "Cantidad que Paga", "Pague X lleve Y Cantidad", JOptionPane.QUESTION_MESSAGE));
				descuento = (float) (pague/lleve);
			}
			else if(tipoPromocionSelected.equals(opcPromocion[3])) //Pague1Segundo
			{
				tipoPromocion = "Pague1Segundo";
				descuento = Float.parseFloat(JOptionPane.showInputDialog (this, "Fraccion que Paga de la Segundo Unidad", "Pague 1 y el Segundo con Descuento", JOptionPane.QUESTION_MESSAGE));
			}
			else if(tipoPromocionSelected.equals(opcPromocion[4])) //PaqueteProducutos
			{
				tipoPromocion = "PaqueteProducutos";
				cantProd = Integer.parseInt(JOptionPane.showInputDialog (this, "Cantidad de Productos que incluye el Paquete", "Paquete de Productos", JOptionPane.QUESTION_MESSAGE));
				pVenta = Integer.parseInt(JOptionPane.showInputDialog (this, "Precio de Venta del Paquete", "Paquete de Productos", JOptionPane.QUESTION_MESSAGE));	
			}
			
			long[] id_Producto = new long[cantProd];
    		int[] stock = new int[cantProd];
    		
    		List nombreProductos = superAndes.darNombreProductos();
    		String[] opcNombreProdutos = new String[nombreProductos.size()];
    		for(int i = 0; i < nombreProductos.size(); i++) {
    			opcNombreProdutos[i] = nombreProductos.get(i).toString();
    		}
    		
			for(int i = 0; i < cantProd; i++) {
				
				JComboBox opcionesNombreProdutos = new JComboBox(opcNombreProdutos);
				JOptionPane.showMessageDialog(null, opcionesNombreProdutos, "Seleccione el Producto: " + (i+1), JOptionPane.QUESTION_MESSAGE);
				String tempString = superAndes.darProductoPorNombre(opcionesNombreProdutos.getSelectedItem().toString()).toString();
				id_Producto[i] = Long.parseLong(tempString.substring(1,tempString.length()-1));
				stock[i] = Integer.parseInt(JOptionPane.showInputDialog (this, "Stock Inical del producto " + (i+1), JOptionPane.QUESTION_MESSAGE));
			}

    		if (nombre != null && fInicio != null && fFin != null && descripcion != null && tipoPromocion != null && !Objects.isNull(id_Sucursal)) {
    			
        		VOPromocion tb1 = superAndes.adicionarPromocion(nombre, fInicio, fFin, descripcion, tipoPromocion, lleve, pague, descuento, pVenta, id_Sucursal);
        		if (tb1 == null)
        		{
        			throw new Exception ("No se pudo crear la Promocion");
        		}
        		String resultado1 = "En adicionar Promocion\n\n";
        		resultado1 += "Promocion adicionada exitosamente: " + tb1;
    			resultado1 += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado1);
    			
    			JOptionPane.showMessageDialog(null, "Ahora se vinculara los productos con la promocion", "Adicionar Promocion", JOptionPane.INFORMATION_MESSAGE);
    			VOPromocionProducto tb2 = null;
    			for(int i = 0; i < cantProd; i++) {
    				
    				tb2 = superAndes.adicionarPromocionProducto(tb1.getId(), id_Producto[i], stock[i], stock[i]);
    			}
    			if (tb2 == null)
        		{
        			throw new Exception ("No se pudo crear la PromocionProducto");
        		}
    			panelDatos.actualizarInterfaz("Se agregaron todas las referncias");
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    
    /* ****************************************************************
	 * 			CRUD de OrdenPedido
	 *****************************************************************/
    
	public void adicionarOrdenPedido( )
    {
    	try
    	{
    		long vTotal = 0;
    		
    		String fCompra = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
    		JOptionPane.showMessageDialog(null, "La fecha de la orden sera la siguiente: " + fCompra, "Adicionar Orden Pedido", JOptionPane.INFORMATION_MESSAGE);

    		int resultProveedor = JOptionPane.showConfirmDialog(this, "Va a registrar un nuevo Proveedor?", "Adicionar Orden Pedido", JOptionPane.YES_NO_OPTION);
    		if(resultProveedor == JOptionPane.YES_OPTION) {
    			adicionarProveedor();
    		}
    		
    		List<Proveedor> proveedores = superAndes.darProveedores();
    		String[] opcNombreProveedores = new String[proveedores.size()];
    		for(int i = 0; i < proveedores.size(); i++) {
    			opcNombreProveedores[i] = proveedores.get(i).getNombre();
    		}
			JComboBox opcionesNombreProveedores = new JComboBox(opcNombreProveedores);
			JOptionPane.showMessageDialog(null, opcionesNombreProveedores, "Seleccione el Proveedor", JOptionPane.QUESTION_MESSAGE);
    		long id_Proveedor = proveedores.get(opcionesNombreProveedores.getSelectedIndex()).getId();

    		long id_Sucursal = id_Sucursal_U;
    		String estado = "pendiente";
    		
    		int resultProducto = JOptionPane.showConfirmDialog(this, "Va a registrar el pedido para un nuevo producto?", "Adicionar Orden Pedido", JOptionPane.YES_NO_OPTION);
    		if(resultProducto == JOptionPane.YES_OPTION) {
    			adicionarProducto();
    		}
    		
    		int cantProd = Integer.parseInt(JOptionPane.showInputDialog (this, "Cantidad de Productos de la Orden de Pedido", "Adicionar Orden Pedido", JOptionPane.QUESTION_MESSAGE));
    		long[] id_Producto = new long[cantProd];
    		long[] cantPorProd = new long[cantProd];
    		long[] precioPorProd = new long[cantProd];
    		
    		List nombreProductos = superAndes.darNombreProductos();
    		String[] opcNombreProdutos = new String[nombreProductos.size()];
    		for(int i = 0; i < nombreProductos.size(); i++) {
    			opcNombreProdutos[i] = nombreProductos.get(i).toString();
    		}
    		
			for(int i = 0; i < cantProd; i++) {
				
				JComboBox opcionesNombreProdutos = new JComboBox(opcNombreProdutos);
				JOptionPane.showMessageDialog(null, opcionesNombreProdutos, "Seleccione el Producto: " + (i+1), JOptionPane.QUESTION_MESSAGE);
				String tempString = superAndes.darProductoPorNombre(opcionesNombreProdutos.getSelectedItem().toString()).toString();
				id_Producto[i] = Long.parseLong(tempString.substring(1,tempString.length()-1));
				cantPorProd[i] = Long.parseLong(JOptionPane.showInputDialog (this, "Cantidad a Pedir " + (i+1), JOptionPane.QUESTION_MESSAGE));
				precioPorProd[i] = Long.parseLong(JOptionPane.showInputDialog (this, "Precio de Compra por unidad " + (i+1), JOptionPane.QUESTION_MESSAGE));
				vTotal += cantPorProd[i] * precioPorProd[i];	
			}
    		
			if (!Objects.isNull(id_Proveedor) && !Objects.isNull(id_Sucursal) && id_Producto != null && cantPorProd != null && precioPorProd != null) {
    			
        		VOOrdenPedido tb1 = superAndes.adicionarOrdenPedido(fCompra, vTotal, estado, id_Proveedor, id_Sucursal);
        		if (tb1 == null)
        		{
        			throw new Exception ("No se pudo crear la Orden de Pedido");
        		}
        		String resultado1 = "En adicionar Orden de Pedido\n\n";
        		resultado1 += "Orden de Pedido adicionada exitosamente: " + tb1;
    			resultado1 += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado1);
    			
    			JOptionPane.showMessageDialog(null, "Ahora se vinculara los productos con la Orden de Pedido", "Adicionar Orden Pedido", JOptionPane.INFORMATION_MESSAGE);
    			VOOrdenPedidoProducto tb2 = null;
    			for(int i = 0; i < cantProd; i++) {
    				
    				tb2 = superAndes.adicionarOrdenPedidoProducto(tb1.getId(), id_Producto[i], cantPorProd[i], precioPorProd[i]);
    			}
    			if (tb2 == null)
        		{
        			throw new Exception ("No se pudo crear la OrdenPedidoProducto");
        		}
    			panelDatos.actualizarInterfaz("Se agregaron todas las referncias");
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
	
	public void registrarOrdenPedido( )
    {
    	try
    	{
    		List<Object> ordenPedidos = superAndes.darOrdenesPedidosPorSucursalYEstado(id_Sucursal_U, "pendiente");
    		String[] opcOrdenPedidos = new String[ordenPedidos.size()];
    		for(int i = 0; i < ordenPedidos.size(); i++) {
    			opcOrdenPedidos[i] = ordenPedidos.get(i).toString();
    		}
			JComboBox opcionesOrdenPedidos = new JComboBox(opcOrdenPedidos);
			JOptionPane.showMessageDialog(null, opcionesOrdenPedidos, "Seleccione el Proveedor", JOptionPane.QUESTION_MESSAGE);
    		long id_OrdenPedido = Long.parseLong(opcionesOrdenPedidos.getSelectedItem().toString());
    		
    		List<Object> ordenPedidoProducto = superAndes.darProductoPorIdOrdenPedido(id_OrdenPedido);
    		List<Object> ordenPedidoCantCompra = superAndes.darCantCompraPorIdOrdenPedido(id_OrdenPedido);
    		System.out.println(ordenPedidoProducto.get(0).toString());
    		System.out.println(ordenPedidoCantCompra.get(0).toString());
    		
			if (ordenPedidoProducto != null && !Objects.isNull(id_OrdenPedido) ) {
				
				
				for(int i = 0; i < ordenPedidoProducto.size(); i++) {
					
					superAndes.actualizarStockBodega(Integer.parseInt(ordenPedidoCantCompra.get(i).toString()), Long.parseLong(ordenPedidoProducto.get(i).toString()));
				}
				
				superAndes.cambiarEsatdo("despachado", id_OrdenPedido);

    			panelDatos.actualizarInterfaz("Se registro la llegada del Pedido y se actualizaron los inventarios");
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
	
	
	/* ****************************************************************
	 * 			CRUD de CarritoCompra
	 *****************************************************************/
	
	public void solicitarCarrito()
    {
		try 
    	{
    		Long id_Sucursal = null;
    		long id_Cliente = Long.parseLong(superAndes.darIdUsuarioPorNDocumento(nDocumento).get(0).toString());
    		id_Cliente_U = id_Cliente;
    		String fCarrito = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
    			
			List sucursales = superAndes.darNombreSucursales();
			String[] opcSucursales = new String[sucursales.size()];
			for(int i = 0; i < sucursales.size(); i++) {
				opcSucursales[i] = sucursales.get(i).toString();
			}
			JComboBox opcionesSucursales = new JComboBox(opcSucursales);
			JOptionPane.showMessageDialog(null, opcionesSucursales, "Seleccione la Sucursales", JOptionPane.QUESTION_MESSAGE);
			id_Sucursal = superAndes.darIdPorSucursal(opcSucursales[opcionesSucursales.getSelectedIndex()]).getId();
			id_Sucursal_U = id_Sucursal;

			if (!Objects.isNull(nDocumento) && !Objects.isNull(id_Sucursal) && fCarrito != null) {
    			
        		VOCarritoCompra tb1 = superAndes.adicionarCarritoCompra(id_Cliente, id_Sucursal, fCarrito, "EnProceso");
        		if (tb1 == null)
        		{
        			throw new Exception ("No se pudo solicitar el Carrito de Compra");
        		}
        		String resultado = "En solicitar el Carrito de Compra\n\n";
        		resultado += "Carrito de Compra solicitado exitosamente: " + tb1;
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
    			id_CarritoCompra = tb1.getId();
    		}
    		else
    		{
    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
    		}
    	}
		catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
	}
	
	public void adicionarAlCarrito( )
    {
		if(!(id_CarritoCompra == 0))
		{
			int cantProd = 0;
			try 
			{
				List nombreProductos = superAndes.darNombreProductos();
				String[] opcNombreProdutos = new String[nombreProductos.size()];
				for(int i = 0; i < nombreProductos.size(); i++) {
					opcNombreProdutos[i] = nombreProductos.get(i).toString();
				}
				JComboBox opcionesNombreProdutos = new JComboBox(opcNombreProdutos);
				JOptionPane.showMessageDialog(null, opcionesNombreProdutos, "Seleccione un Producto: ", JOptionPane.QUESTION_MESSAGE);		
				String aux_idLote = superAndes.darProductoPorNombre(opcionesNombreProdutos.getSelectedItem().toString()).toString();
				String idLote = aux_idLote.substring(1, aux_idLote.length() - 1);
				String aux_pVenta = superAndes.darPrecioPorId(Long.parseLong(idLote)).toString();
				String pVenta = aux_pVenta.substring(1, aux_pVenta.length() - 1);
				cantProd = Integer.parseInt(JOptionPane.showInputDialog (this, "Cantidad a llevar", JOptionPane.QUESTION_MESSAGE));
				
				if (idLote != null && pVenta != null && !Objects.isNull(cantProd)) {
					
		    		VOCarritoCompraProducto tb1 = superAndes.adicionarCarritoCompraProducto(id_CarritoCompra, Long.parseLong(idLote), Integer.parseInt(pVenta), cantProd);
		    		if (tb1 == null)
		    		{
		    			throw new Exception ("No se pudo adicionar producto al Carrito de Compra");
		    		}
		    		superAndes.actualizarStockEstante(-cantProd, Long.parseLong(idLote));
					prod.add(idLote);
					cant.add(cantProd);
					pVentaH.add(Integer.parseInt(pVenta));
		    		String resultado = "En adicionar producto al Carrito de Compra\n\n";
		    		resultado += "Producto adicinado exitosamente y stock del estante actualizado: " + tb1;
					resultado += "\n Operación terminada";
					panelDatos.actualizarInterfaz(resultado);
				}
				else
				{
					panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
				}
			}
			catch (Exception e)
			{
				String resultado = generarMensajeError(e);
				panelDatos.actualizarInterfaz(resultado);
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "No se ha solicitado ningun Carrito", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
		
	public void devolverDelCarrito( )
	{
		if(!(id_CarritoCompra == 0))
		{
			if(!prod.isEmpty())
			{
				try 
				{
					String[] opcNombreProdutos = new String[prod.size()];
					for(int i = 0; i < prod.size(); i++) {
						String aux = superAndes.darNombrePorId(Long.parseLong(prod.get(i))).toString();    			
						opcNombreProdutos[i] = aux.substring(1, aux.length() - 1);
					}
					JComboBox opcionesNombreProdutos = new JComboBox(opcNombreProdutos);
					JOptionPane.showMessageDialog(null, opcionesNombreProdutos, "Seleccione Producto a Devolver: ", JOptionPane.QUESTION_MESSAGE);
					
					if (!Objects.isNull(id_CarritoCompra) && !Objects.isNull(prod.get(opcionesNombreProdutos.getSelectedIndex()))) {
			
						long tbEliminados = superAndes.eliminarCarritoCompraProductoPorIds(id_CarritoCompra, Long.parseLong(prod.get(opcionesNombreProdutos.getSelectedIndex())));
						String resultado = "En eliminar Producto del Carrito\n\n";
						resultado += tbEliminados + " Producto eliminado\n";
						resultado += "\n Operación terminada";
						panelDatos.actualizarInterfaz(resultado);
						superAndes.actualizarStockEstante(cant.get(opcionesNombreProdutos.getSelectedIndex()), Long.parseLong(prod.get(opcionesNombreProdutos.getSelectedIndex())));
						prod.remove(opcionesNombreProdutos.getSelectedIndex());
						cant.remove(opcionesNombreProdutos.getSelectedIndex());
						pVentaH.remove(opcionesNombreProdutos.getSelectedIndex());
					}
					else
					{
						panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
					}			
					
				} catch (Exception e)
				{
					String resultado = generarMensajeError(e);
					panelDatos.actualizarInterfaz(resultado);
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "El Carrito esta vacio", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "No se ha solicitado ningun Carrito", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void pagarCompra( )
	{
		if(!(id_CarritoCompra == 0))
		{
			if(!prod.isEmpty())
			{
				try 
		    	{
		    		int totalCompra = 0;
		    		long tbEliminadosS = 0;
		    		
		    		for(int i = 0; i < prod.size(); i++) {
		    			totalCompra =+ (pVentaH.get(i)*cant.get(i));
		    		}
		    		
		    		int resultPagar = JOptionPane.showConfirmDialog(this, "El Total de la Compra es: " + totalCompra, "Pagar Compra?", JOptionPane.YES_NO_OPTION);
		    		if(resultPagar == JOptionPane.YES_OPTION) {
		    			
		        		for(int i = 0; i < prod.size(); i++) {
		        			tbEliminadosS =+ superAndes.actualizarStockTotal(-cant.get(i), Long.parseLong(prod.get(i)));
		        		}
		        		
		        		long tbActualizadosC = superAndes.actualizarEstadoCarrito(id_CarritoCompra, "Ejecutado");
		        		
		        		// Valor aleatorio para escojer el cajero (entre M y N, ambos incluidos)
		        		int M = 0;
		        		int N = superAndes.darCajerosPorSucursal(id_Sucursal_U).size()-1;
		        		Double index = Math.floor(Math.random()*(N-M+1)+M);
		        		System.out.println(index);
		        		List<Object> pruebaList = superAndes.darCajerosPorSucursal(id_Sucursal_U);
		        		String id_Cajero = pruebaList.get(index.intValue()).toString();
		        		System.out.println(id_Cajero);
		        		String fVenta = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
		        		
		        		System.out.println(fVenta);
		        		System.out.println(totalCompra);
		        		System.out.println(id_Sucursal_U);
		        		System.out.println(id_Cajero);
		        		System.out.println(id_Cliente_U);
		        		System.out.println(id_CarritoCompra);
		        		
		        		VOVenta tb1 = superAndes.adicionarVenta(fVenta, totalCompra, id_Sucursal_U, Long.parseLong(id_Cajero), id_Cliente_U, id_CarritoCompra);
		        		if (tb1 == null)
		        		{
		        			throw new Exception ("No se pudo venta");
		        		}
		
						String resultado = "Actualizando Stock Total y estado del Carrito:\n\n";
		    			resultado += tbEliminadosS + " Stocks actualizados\n";
		    			resultado += tbActualizadosC + " Estado del Carrito actualizado\n\n";
		    			resultado += "Adicionando Venta:\n\n";
		    			resultado += "Venta adicinada exitosamente: " + tb1;
		    			resultado += "\n\n Operación terminada";
		    			panelDatos.actualizarInterfaz(resultado);
		    		}
		    		else
		    		{
		    			panelDatos.actualizarInterfaz("Operación cancelada por el usuario");
		    		}			
		    	} catch (Exception e)
				{
					String resultado = generarMensajeError(e);
					panelDatos.actualizarInterfaz(resultado);
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "El Carrito esta vacio", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "No se ha solicitado ningun Carrito", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void abandonarCarrito( )
	{
		if(!(id_CarritoCompra == 0))
		{
			try
			{
				long tbActualizados = superAndes.actualizarEstadoCarrito(id_CarritoCompra, "Abandonado");
				String resultado = "Actualizando estado del Carrito:\n\n";
    			resultado += tbActualizados + " Estado del Carrito actualizado\n";
    			resultado += "\n Operación terminada";
    			panelDatos.actualizarInterfaz(resultado);
				
			} catch (Exception e) {
				String resultado = generarMensajeError(e);
				panelDatos.actualizarInterfaz(resultado);
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "No se ha solicitado ningun Carrito", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void recolectarCarritosAbandonados( )
	{
		try	{
			List<Object> carritos = superAndes.darCarritosComprasPorSucursalYEstado(id_Sucursal_U, "Abandonado");
			
			for (int i = 0; i < carritos.size(); i++) {
				
				List<Object> prod = superAndes.darProdPorIdCarrito(Long.parseLong(carritos.get(i).toString()));
				List<Object> cant = superAndes.darCantPorIdCarrito(Long.parseLong(carritos.get(i).toString()));
	
				if(prod.size() != 0)
				{
					for (int j = 0; j < prod.size(); j++)
					{
						superAndes.actualizarStockEstante(Integer.parseInt(cant.get(j).toString()), Long.parseLong(prod.get(j).toString()));
					}
				}
				superAndes.actualizarEstadoCarrito(Long.parseLong(carritos.get(i).toString()), "Recolectado");
			}
			
			String resultado = "Actualizando estado del los Carrito Abandonados:\n\n";
			resultado += carritos.size() + " Estado de Carritos actualizados y sus Productos devueltos a los Estantes\n";
			resultado += "\n Operación terminada";
			panelDatos.actualizarInterfaz(resultado);

			
		} catch (Exception e) {
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
		
	}

    /* ****************************************************************
	 * 			Requerimientos funcionales de consulta
	 *****************************************************************/

    public void requerimientoFuncional1()
    {
    	ReqFuncionalDlg1 dialogoReq1 = new ReqFuncionalDlg1();
    	dialogoReq1.setVisible(true);
    	
    	if(dialogoReq1.getFechaIncio() != null)
    	{
    		String resultado = "";//"Fecha inicio: " + dialogoReq1.getFechaIncio().toString() + ", Fecha fin: " + dialogoReq1.getFechaFin().toString();
    		List<Object[]> sucursales = superAndes.darSucursalesReq1();
    		
    		for(int i = 0; i < sucursales.size(); i++)
    		{
    			Object[] sucursal = sucursales.get(i);
    			resultado += "\n" + sucursal[0] + "\t" + sucursal[1] + "\t" + sucursal[2];
    			System.out.println(sucursal.toString());
    		}
        	panelDatos.actualizarInterfaz(resultado);
    	}
    }
    
    public void requerimientoFuncional2()
    {
    	List<Object[]> promociones = superAndes.darPromocionesReq2();
    	String resultado = "";
    	
    	for(int i = 0; i < promociones.size(); i++)
		{
			Object[] promocion = promociones.get(i);
			
			for(int j = 0; j < promocion.length; j++)
			{
				resultado += promocion[j] + "\t";
			}
			
			resultado += "\n";
		}
    	panelDatos.actualizarInterfaz(resultado);
    }
    
    public void requerimientoFuncional3()
    {
    	
    	
    	try 
    	{
    		List sucursales = superAndes.darNombreSucursales();
    		String[] opcSucursales = new String[sucursales.size()];
    		for(int i = 0; i < sucursales.size(); i++) {
    			opcSucursales[i] = sucursales.get(i).toString();
    		}
			JComboBox opcionesSucursales = new JComboBox(opcSucursales);
			
			JOptionPane.showMessageDialog(null, opcionesSucursales, "Seleccione la sucursal", JOptionPane.QUESTION_MESSAGE);
			long id_sucursal = superAndes.darIdPorSucursal(opcSucursales[opcionesSucursales.getSelectedIndex()]).getId();
    		
			List<Object[]> bodegas = superAndes.darBodegasReq3(id_sucursal);
			List<Object[]> estantes = superAndes.darEstantesReq3(id_sucursal);
			
			String resultado = "Consulta para la sucursal " + id_sucursal + " - " + opcSucursales[opcionesSucursales.getSelectedIndex()] + "\n";
			resultado += "INDICES DE OCUPACIÓN POR BODEGA\n";
			resultado += "IDBODEGA\tPESOMAX\tVOLMAX\tTIPO_ALMACEN\tPORCENTAJE_VOL\tPORCENTAJE_PESO\n";
			
			for(int i = 0; i < bodegas.size(); i++)
			{
				Object[] bodega = bodegas.get(i);
				
				for(int j = 0; j < bodega.length; j++)
				{
					resultado += bodega[j] + "\t";
				}
				
				resultado += "\n";
			}
			
			resultado += "\nINDICES DE OCUPACIÓN POR ESTANTE\n";
			resultado += "IDESTANTE\tPESOMAX\tVOLMAX\tTIPO_ALMACEN\tPORCENTAJE_VOL\tPORCENTAJE_PESO\n";
			
			for(int i = 0; i < estantes.size(); i++)
			{
				Object[] estante = estantes.get(i);
				
				for(int j = 0; j < estante.length; j++)
				{
					resultado += estante[j] + "\t";
				}
				
				resultado += "\n";
			}
			
			panelDatos.actualizarInterfaz(resultado);
		} 
    	catch (Exception e) 
    	{
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
    }
    
    public void requerimientoFuncional4()
    {
    	ReqFuncionalDlg1 dialogoReq1 = new ReqFuncionalDlg1();
    	dialogoReq1.setVisible(true);
    	
    	if(dialogoReq1.getFechaIncio() != null)
    	{
    		String resultado = "";//"Fecha inicio: " + dialogoReq1.getFechaIncio().toString() + ", Fecha fin: " + dialogoReq1.getFechaFin().toString();
    		List<Object[]> sucursales = superAndes.darSucursalesReq1();;;;
    		
    		for(int i = 0; i < sucursales.size(); i++)
    		{
    			Object[] sucursal = sucursales.get(i);
    			resultado += "\n" + sucursal[0] + "\t" + sucursal[1] + "\t" + sucursal[2];
    			System.out.println(sucursal.toString());
    		}
        	panelDatos.actualizarInterfaz(resultado);
    	}
    }
    
    public void requerimientoFuncional5()
    {
    	ReqFuncionalDlg1 dialogoReq1 = new ReqFuncionalDlg1();
    	dialogoReq1.setVisible(true);
    	
    	if(dialogoReq1.getFechaIncio() != null)
    	{
    		String resultado = "";//"Fecha inicio: " + dialogoReq1.getFechaIncio().toString() + ", Fecha fin: " + dialogoReq1.getFechaFin().toString();
    		List<Object[]> sucursales = superAndes.darSucursalesReq1();
    		
    		for(int i = 0; i < sucursales.size(); i++)
    		{
    			Object[] sucursal = sucursales.get(i);
    			resultado += "\n" + sucursal[0] + "\t" + sucursal[1] + "\t" + sucursal[2];
    			System.out.println(sucursal.toString());
    		}
        	panelDatos.actualizarInterfaz(resultado);
    	}
    }
    
    public void requerimientoFuncional6()
    {
    	ReqFuncionalDlg1 dialogoReq1 = new ReqFuncionalDlg1();
    	dialogoReq1.setVisible(true);
    	
    	if(dialogoReq1.getFechaIncio() != null)
    	{
    		String resultado = "";//"Fecha inicio: " + dialogoReq1.getFechaIncio().toString() + ", Fecha fin: " + dialogoReq1.getFechaFin().toString();
    		List<Object[]> sucursales = superAndes.darSucursalesReq1();
    		
    		for(int i = 0; i < sucursales.size(); i++)
    		{
    			Object[] sucursal = sucursales.get(i);
    			resultado += "\n" + sucursal[0] + "\t" + sucursal[1] + "\t" + sucursal[2];
    			System.out.println(sucursal.toString());
    		}
        	panelDatos.actualizarInterfaz(resultado);
    	}
    }


	/* ****************************************************************
	 * 			Métodos administrativos
	 *****************************************************************/
	/**
	 * Muestra el log de SuperAndes
	 */
	public void mostrarLogSuperAndes ()
	{
		mostrarArchivo ("SuperAndes.log");
	}
	
	/**
	 * Muestra el log de datanucleus
	 */
	public void mostrarLogDatanuecleus ()
	{
		mostrarArchivo ("datanucleus.log");
	}
	
	/**
	 * Limpia el contenido del log de SuperAndes
	 * Muestra en el panel de datos la traza de la ejecución
	 */
	public void limpiarLogSuperAndes ()
	{
		// Ejecución de la operación y recolección de los resultados
		boolean resp = limpiarArchivo ("SuperAndes.log");

		// Generación de la cadena de caracteres con la traza de la ejecución de la demo
		String resultado = "\n\n************ Limpiando el log de SuperAndes ************ \n";
		resultado += "Archivo " + (resp ? "limpiado exitosamente" : "NO PUDO ser limpiado !!");
		resultado += "\nLimpieza terminada";

		panelDatos.actualizarInterfaz(resultado);
	}
	
	/**
	 * Limpia el contenido del log de datanucleus
	 * Muestra en el panel de datos la traza de la ejecución
	 */
	public void limpiarLogDatanucleus ()
	{
		// Ejecución de la operación y recolección de los resultados
		boolean resp = limpiarArchivo ("datanucleus.log");

		// Generación de la cadena de caracteres con la traza de la ejecución de la demo
		String resultado = "\n\n************ Limpiando el log de datanucleus ************ \n";
		resultado += "Archivo " + (resp ? "limpiado exitosamente" : "NO PUDO ser limpiado !!");
		resultado += "\nLimpieza terminada";

		panelDatos.actualizarInterfaz(resultado);
	}
	
	/**
	 * Limpia todas las tuplas de todas las tablas de la base de datos de SuperAndes
	 * Muestra en el panel de datos el número de tuplas eliminadas de cada tabla
	 */
	public void limpiarBD ()
	{
		try 
		{
    		// Ejecución de la demo y recolección de los resultados
			long eliminados [] = superAndes.limpiarSuperAndes();
			
			// Generación de la cadena de caracteres con la traza de la ejecución de la demo
			String resultado = "\n\n************ Limpiando la base de datos ************ \n";
			resultado += eliminados [0] + " Bodegas eliminadas\n";
			resultado += eliminados [1] + " Carrito de Compra eliminados\n";
			resultado += eliminados [2] + " Carrito de Compra Porductos eliminados\n";
			resultado += eliminados [3] + " Estantes eliminados\n";
			resultado += eliminados [4] + " Facturas Electronicas eliminadas\n";
			resultado += eliminados [5] + " Ordenes de Pedidos eliminados\n";
			resultado += eliminados [6] + " Ordenes de Pedidos y Productos eliminados\n";
			resultado += eliminados [7] + " Productos eliminados\n";
			resultado += eliminados [8] + " Promociones eliminadas\n";
			resultado += eliminados [9] + " Promociones y Productos eliminados\n";
			resultado += eliminados [10] + " Proveedores eliminados\n";
			resultado += eliminados [11] + " Sucursales eliminadas\n";
			resultado += eliminados [12] + " Supermercados eliminados\n";
			resultado += eliminados [13] + " Tipos de Productos eliminados\n";
			resultado += eliminados [14] + " Tipos de Usuarios eliminados\n";
			resultado += eliminados [15] + " Usuarios eliminados\n";
			resultado += eliminados [16] + " Ventas eliminadas\n";
			resultado += "\nLimpieza terminada";
   
			panelDatos.actualizarInterfaz(resultado);
		} 
		catch (Exception e) 
		{
//			e.printStackTrace();
			String resultado = generarMensajeError(e);
			panelDatos.actualizarInterfaz(resultado);
		}
	}
	
	/**
	 * Muestra el modelo conceptual de SuperAndes
	 */
	public void mostrarModeloConceptual ()
	{
		mostrarArchivo ("data/Modelo Conceptual SuperAndes.pdf");
	}
	
	/**
	 * Muestra el esquema de la base de datos de SuperAndes
	 */
	public void mostrarEsquemaBD ()
	{
		mostrarArchivo ("data/Esquema BD SuperAndes.pdf");
	}
	
	/**
	 * Muestra el script de creación de la base de datos
	 */
	public void mostrarScriptBD ()
	{
		mostrarArchivo ("data/EsquemaSuperAndes.sql");
	}
	
	/**
	 * Muestra el script de creación de la base de datos
	 */
	public void mostrarScriptLimpiezaBD ()
	{
		mostrarArchivo ("data/LimpiezaSuperAndes.sql");
	}
	
	/**
     * Muestra la información acerca del desarrollo de esta apicación
     */
    public void acercaDe ()
    {
		String resultado = "\n\n ************************************\n\n";
		resultado += " * Universidad	de	los	Andes	(Bogotá	- Colombia)\n";
		resultado += " * Departamento	de	Ingeniería	de	Sistemas	y	Computación\n";
		resultado += " * Licenciado	bajo	el	esquema	Academic Free License versión 2.1\n";
		resultado += " * \n";		
		resultado += " * Curso: isis2304 - Sistemas Transaccionales\n";
		resultado += " * Proyecto: SuperAndes Uniandes\n";
		resultado += " * @version 1.0\n";
		resultado += " * @author Germán Bravo\n";
		resultado += " * Julio de 2018\n";
		resultado += " * \n";
		resultado += " * Revisado por: Claudia Jiménez, Christian Ariza\n";
		resultado += "\n ************************************\n\n";

		panelDatos.actualizarInterfaz(resultado);		
    }
    

	/* **********************************************************************************
	 * 			Métodos privados para la presentación de resultados y otras operaciones
	 ***********************************************************************************/
    /**
     * Genera una cadena de caracteres con la lista de los tipos de bebida recibida: una línea por cada tipo de bebida
     * @param lista - La lista con los tipos de bebida
     * @return La cadena con una líea para cada tipo de bebida recibido
     */
	private String listarProveedores(List<VOProveedor> lista) 
    {
    	String resp = "Los proveedores existentes son:\n";
    	int i = 1;
        for (VOProveedor tb : lista)
        {
        	resp += i++ + ". " + tb.toString() + "\n";
        }
        return resp;
	}
	
	private String listarTiposProductos(List<VOTipoProducto> lista) 
    {
    	String resp = "Los tipos de productos existentes son:\n";
    	int i = 1;
        for (VOTipoProducto tb : lista)
        {
        	resp += i++ + ". " + tb.toString() + "\n";
        }
        return resp;
	}
	
	private String listarTiposUsuario(List<VOTipoUsuario> lista) 
    {
    	String resp = "Los tipos de usuarios existentes son:\n";
    	int i = 1;
        for (VOTipoUsuario tb : lista)
        {
        	resp += i++ + ". " + tb.toString() + "\n";
        }
        return resp;
	}
	
	private String listarSucursales(List<VOSucursal> lista) 
    {
    	String resp = "Las sucursales existentes son:\n";
    	int i = 1;
        for (VOSucursal tb : lista)
        {
        	resp += i++ + ". " + tb.toString() + "\n";
        }
        return resp;
	}
	
	private String listarUsuarios(List<VOUsuario> lista) 
    {
    	String resp = "Los usuarios existentes son:\n";
    	int i = 1;
        for (VOUsuario tb : lista)
        {
        	resp += i++ + ". " + tb.toString() + "\n";
        }
        return resp;
	}
	
	private String listarProductos(List<VOProducto> lista) 
    {
    	String resp = "Los Productos existentes son:\n";
    	int i = 1;
        for (VOProducto tb : lista)
        {
        	resp += i++ + ". " + tb.toString() + "\n";
        }
        return resp;
	}


    /**
     * Genera una cadena de caracteres con la descripción de la excepcion e, haciendo énfasis en las excepcionsde JDO
     * @param e - La excepción recibida
     * @return La descripción de la excepción, cuando es javax.jdo.JDODataStoreException, "" de lo contrario
     */
	private String darDetalleException(Exception e) 
	{
		String resp = "";
		if (e.getClass().getName().equals("javax.jdo.JDODataStoreException"))
		{
			JDODataStoreException je = (javax.jdo.JDODataStoreException) e;
			return je.getNestedExceptions() [0].getMessage();
		}
		return resp;
	}

	/**
	 * Genera una cadena para indicar al usuario que hubo un error en la aplicación
	 * @param e - La excepción generada
	 * @return La cadena con la información de la excepción y detalles adicionales
	 */
	private String generarMensajeError(Exception e) 
	{
		String resultado = "************ Error en la ejecución\n";
		resultado += e.getLocalizedMessage() + ", " + darDetalleException(e);
		resultado += "\n\nRevise datanucleus.log y SuperAndes.log para más detalles";
		return resultado;
	}

	/**
	 * Limpia el contenido de un archivo dado su nombre
	 * @param nombreArchivo - El nombre del archivo que se quiere borrar
	 * @return true si se pudo limpiar
	 */
	private boolean limpiarArchivo(String nombreArchivo) 
	{
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new FileWriter(new File (nombreArchivo)));
			bw.write ("");
			bw.close ();
			return true;
		} 
		catch (IOException e) 
		{
//			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Abre el archivo dado como parámetro con la aplicación por defecto del sistema
	 * @param nombreArchivo - El nombre del archivo que se quiere mostrar
	 */
	private void mostrarArchivo (String nombreArchivo)
	{
		try
		{
			Desktop.getDesktop().open(new File(nombreArchivo));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/* ****************************************************************
	 * 			Métodos de la Interacción
	 *****************************************************************/
    /**
     * Método para la ejecución de los eventos que enlazan el menú con los métodos de negocio
     * Invoca al método correspondiente según el evento recibido
     * @param pEvento - El evento del usuario
     */
    @Override
	public void actionPerformed(ActionEvent pEvento)
	{
		String evento = pEvento.getActionCommand( );		
        try 
        {
			Method req = InterfazSuperAndesApp.class.getMethod ( evento );			
			req.invoke ( this );
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		} 
	}
    
	/* ****************************************************************
	 * 			Programa principal
	 *****************************************************************/
    /**
     * Este método ejecuta la aplicación, creando una nueva interfaz
     * @param args Arreglo de argumentos que se recibe por línea de comandos
     */
    public static void main( String[] args )
    {
        try
        {
            // Unifica la interfaz para Mac y para Windows.
            UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName( ) );
            InterfazSuperAndesApp interfaz = new InterfazSuperAndesApp( );
			if(interfaz.permitirIngreso)
			{
				interfaz.setVisible( true );
			} 
			else
			{
				interfaz.dispose();
			}

        }
        catch( Exception e )
        {
            e.printStackTrace( );
        }
    }
}
