<#if keystoreNode??>
${keystoreNode?replace('store://?', '', 'r')}
</#if>
<#if licenseNode??>
${licenseNode?replace('store://?', '', 'r')}
</#if>
<#list dictionaryNodes as d>
${d?replace('store://?', '', 'r')}
</#list>
<#list surfConfigNodes as sc>
${sc?replace('store://?', '', 'r')}
</#list>
<#list siteNodes as s>
${s?replace('store://?', '', 'r')}
</#list>
<#list personNodes as p>
${p?replace('store://?', '', 'r')}
</#list>