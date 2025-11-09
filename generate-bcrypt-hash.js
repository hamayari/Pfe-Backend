// Script pour générer un hash bcrypt pour n8n

const bcrypt = require('bcrypt');
const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

console.log('🔐 Générateur de hash bcrypt pour n8n');
console.log('=====================================\n');

rl.question('Entrez le mot de passe que vous voulez utiliser: ', (password) => {
    if (password.length < 8) {
        console.log('\n❌ Le mot de passe doit contenir au moins 8 caractères');
        rl.close();
        return;
    }
    
    console.log('\n🔄 Génération du hash...\n');
    
    const hash = bcrypt.hashSync(password, 10);
    
    console.log('✅ Hash généré avec succès!\n');
    console.log('📝 Informations:');
    console.log('   Mot de passe:', password);
    console.log('   Hash bcrypt:', hash);
    console.log('\n📋 Instructions:');
    console.log('1. Ouvrez DB Browser for SQLite');
    console.log('2. Ouvrez: ' + process.env.USERPROFILE + '\\.n8n\\database.sqlite');
    console.log('3. Allez dans "Browse Data" > Table "user"');
    console.log('4. Double-cliquez sur le champ "password"');
    console.log('5. Remplacez par le hash ci-dessus');
    console.log('6. Cliquez sur "Write Changes"');
    console.log('7. Connectez-vous avec votre email et le nouveau mot de passe\n');
    
    rl.close();
});
